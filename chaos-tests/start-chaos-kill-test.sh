#!/bin/bash

# set -e to exit if the time provided by argument 2 is not valid for date.
# The variable STOP_TIME will store the seconds since 1970-01-01 00:00:00
# UTC, according to the date specified by -d "$2".

# $ ./start-chaos-kill-test.sh "1 hour 4 minutes 3 seconds"
# Starting test at Fri Jun  2 10:50:28 BRT 2017
# Expected finish time: Fri Jun  2 11:54:31 BRT 2017
# Using chaos docker-compose file: chaos-kill-peers.yml

# $ ./start-chaos-kill-test.sh "tomorrow 8:00am"
# Starting test at Fri Jun  2 10:50:39 BRT 2017
# Expected finish time: Sat Jun  3 08:00:00 BRT 2017
# Using chaos docker-compose file: chaos-kill-peers.yml

# $ ./start-chaos-kill-test.sh "monday 8:00am"
# Starting test at Fri Jun  2 10:51:25 BRT 2017
# Expected finish time: Mon Jun  5 08:00:00 BRT 2017
# Using chaos docker-compose file: chaos-kill-peers.yml

set -e
STOP_TIME=$2
STOP_TIME_EPOCH_SECONDS=$(date -d "$STOP_TIME" "+%s")
set +e
KILL_COMPOSE_FILE=$1
echo -e "Starting test at $(date)"
echo -e "Expected finish time: $(date -d "$STOP_TIME")"
echo -e "Using chaos docker-compose file: $KILL_COMPOSE_FILE"
echo -e 'Starting catapult server...'
CATAPULT_COMPOSE_FILE=$(python3 utils.py get_relative_file_path --file_name=docker-compose-auto-recovery.yml)
# set -x
# CHAOS_LOG_FILE=chaos-logs/$KILL_COMPOSE_FILE.$(date +"%d.%m.%Y-%H.%M.%S").log
# Launch the catapult server and pumba containers
docker-compose -f ${CATAPULT_COMPOSE_FILE} -f ${KILL_COMPOSE_FILE} up -d

# Call the python script and then remove the square braces [] from the output
DOCKER_CONTAINERS=($(python3 utils.py get_docker_container_names --compose_file=$KILL_COMPOSE_FILE))
echo "List of containers: ${DOCKER_CONTAINERS[@]}"
sleep 10
docker ps
echo 'Finished starting up catapult server.'

# Edit the config-node.properties to set the values of trustedHost and localNetworks to empty values

echo 'Getting private key and generation hash...'
# Get the private key and generation hash
PRIVATE_KEY=$(python3 utils.py get_first_user_private_key) # 957487744B5808B719620946E0B1F2E375A163C5E7007DA63A8F140945A9DE58
GEN_HASH=$(curl localhost:3000/node/info | jq '.networkGenerationHash') # 13A29782C498085AF186E2E93C09DB8E0EA4B130D9CF537181950F6E6344F1CB
TRANSACTIONS_PER_SEC=$3
NUM_ACCOUNTS=10000 # $(expr $3 * $STOP_TIME_EPOCH_SECONDS)
# Start the spammer tool with required args to send transactions at this catapult server
# Assume that every chaos testing env. is going to have access to private docker images
cp -rvf ../catapult-spammer/cmds/bootstrap/dockerfiles/nemgen/* ../catapult-service-bootstrap/cmds/bootstrap/dockerfiles/nemgen
cd ../catapult-service-bootstrap/cmds/bootstrap
docker build -f dockerfiles/nemgen/Dockerfile -t chaos-spammer:latest dockerfiles/nemgen/
docker run --rm --stop-signal SIGINT \
  --name chaos-spammer_1 \
  -v "`pwd`"/build/catapult-config/peer-node-0/userconfig/resources/:/userconfig/resources/ \
  -v "`pwd`"/bin/bash:/bin-mount \
  --detach chaos-spammer:latest bash -c "sleep 1000000"
docker exec --detach chaos-spammer_1 "/usr/catapult/bin/catapult.tools.spammer --resources /userconfig --prepare --total ${NUM_ACCOUNTS}; /usr/catapult/bin/catapult.tools.spammer --resources /userconfig --rate=${TRANSACTIONS_PER_SEC} --total=${NUM_ACCOUNTS} --command=seed --spammingAccountKey=${PRIVATE_KEY} --base=${NUM_ACCOUNTS} --clientPrivateKey=${PRIVATE_KEY} --networkGenerationHash=${GEN_HASH}"

cd ../../../chaos-tests

# Repeat the loop while the current date is less than STOP_TIME_EPOCH_SECONDS
while [ $(date "+%s") -lt ${STOP_TIME_EPOCH_SECONDS} ]; do
  sleep 60
  for container in "${DOCKER_CONTAINERS[@]}"; do
    # Remove the single quotes from the container name string (it was returned by python with '')
    container=$(sed s/\'//g <<<$container)
    echo "Processing docker container: $container"
    NODE_STATUS=$(docker inspect $container --format='{{.State.Status}}')
    NODE_EXITCODE=$(docker inspect $container --format='{{.State.ExitCode}}')
    echo "container $container status: ${NODE_STATUS}; exitcode: ${NODE_EXITCODE}"
    if [ ${NODE_STATUS} == "exited"  ] && [ ${NODE_EXITCODE} == "137" ]; then
      echo "$container was randomly killed by pumba, restarting it."
      docker start $container
    fi
  done
done

# Now, query the catapult mongo db and check the count of transactions
# MONGO_DATABASES=($(python3 mongo_query.py | tr -d '[],'))
# echo "List of databases in mongodb: ${MONGO_DATABASES[@]}"
python3 mongo_query.py

# Stop spammer container and remove it if not automatically removed
docker stop chaos-spammer_1; docker rm chaos-spammer_1

# Stop catapult
docker-compose -f ${CATAPULT_COMPOSE_FILE} -f ${KILL_COMPOSE_FILE} down --remove-orphans
# Delete all data and settings created by catapult for a clean start next time
# ???how to run the below command without the prompt for password during automated testing???
#cd ../catapult-service-bootstrap && sudo cmds/clean-all && cd ../chaos-tests