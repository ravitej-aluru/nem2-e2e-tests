#!/bin/bash

# https://stackoverflow.com/questions/11176284/time-condition-loop-in-shell
# The variable STOP_TIME will store the seconds since 1970-01-01 00:00:00
# UTC, according to the date specified by -d "$2".

# $ ./start-chaos-kill-test.sh ... "1 hour 4 minutes 3 seconds" ...
# $ ./start-chaos-kill-test.sh ... "tomorrow 8:00am" ...
# $ ./start-chaos-kill-test.sh ... "monday 8:00am" ...

# set -e to exit if the time provided by argument 2 is not valid for date.
set -e
STOP_TIME=$2
set +e

CHAOS_COMPOSE_FILE=$1
echo -e "Using chaos docker-compose file: $CHAOS_COMPOSE_FILE"
SYMBOL_COMPOSE_FILE=$(python3 utils.py get_relative_file_path --file_name=docker-compose-auto-recovery.yml --target_dir=catapult-service-bootstrap)
SPAMMER_COMPOSE_FILE=$(python3 utils.py get_relative_file_path --file_name=docker-compose-spammer.yml --target_dir=catapult-spammer)

# set -x
# CHAOS_LOG_FILE=chaos-logs/$CHAOS_COMPOSE_FILE.$(date +"%d.%m.%Y-%H.%M.%S").log
# Launch the symbol server and pumba containers
echo -e 'Starting Symbol server...'
docker-compose -f ${SYMBOL_COMPOSE_FILE} -f ${CHAOS_COMPOSE_FILE} up -d

# Call the python script
DOCKER_CONTAINERS=($(python3 utils.py get_docker_container_names --compose_file=$CHAOS_COMPOSE_FILE))
echo "List of containers: ${DOCKER_CONTAINERS[@]}"
sleep 10
docker ps
echo 'Finished starting up catapult server.'

echo 'Getting private key and generation hash...'
# Get the private key and generation hash from the started symbol server
NUM_ACCOUNTS=10000 # $(expr $3 * $STOP_TIME_EPOCH_SECONDS)

export PRIVATE_KEY=$(python3 utils.py get_first_user_private_key)
export GENERATION_HASH=$(curl localhost:3000/node/info | jq --raw-output '.networkGenerationHash')
export NUMBER_OF_ACCOUNTS=$NUM_ACCOUNTS
export TRANSACTIONS_PER_SECOND=$3

echo 'Creating .env file with private key and generation hash from the catapult config...'
envsubst < ../catapult-spammer/cmds/bootstrap/spammer/spammer.env > ../catapult-spammer/cmds/bootstrap/.env
echo 'printing ../catapult-spammer/cmds/bootstrap/.env file contents...'
cat ../catapult-spammer/cmds/bootstrap/.env

printf '\n\nNumber of transactions in mongodb before starting spammer...'
python3 mongo_query.py

docker-compose -f ${SPAMMER_COMPOSE_FILE} up -d --build
echo "Printing spammer container logs..." && sleep 10
docker-compose -f ${SPAMMER_COMPOSE_FILE} logs spammer
# Properly check if spammer container started and is actually sending transactions. 
# If not, exit, since there is no point in continuing
# Also, could improve this by monitoring the spammer container too to check it has not exited
echo "Spammer started; entering peer containers monitoring loop..."

STOP_TIME_EPOCH_SECONDS=$(date -d "$STOP_TIME" "+%s")
echo -e "Expected finish time: $(date -d "$STOP_TIME")"

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

# Now query the catapult mongo db and check the count of transactions
printf '\n\nNumber of transactions in mongodb after the test...'
python3 mongo_query.py

# Stop spammer
docker-compose -f ${SPAMMER_COMPOSE_FILE} down

# Stop symbol and pumba
docker-compose -f ${SYMBOL_COMPOSE_FILE} -f ${CHAOS_COMPOSE_FILE} down --remove-orphans

# Delete all data and settings created by symbol for a clean start next time
# passwordless sudo user will be created on the chaos net instances for below to work
# cd ../catapult-service-bootstrap && sudo ./cmds/clean-all && cd ../chaos-tests