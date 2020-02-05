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

# set -x
# CHAOS_LOG_FILE=chaos-logs/$KILL_COMPOSE_FILE.$(date +"%d.%m.%Y-%H.%M.%S").log
# Launch the catapult server and pumba containers
docker-compose -f ../catapult-bootstrap/cmds/docker/docker-compose.yml -f ${KILL_COMPOSE_FILE} up -d
# Call the python script and then remove the square braces [] from the output
DOCKER_CONTAINERS=($(python3 name_parser.py $KILL_COMPOSE_FILE | tr -d '[],'))
echo "List of containers: ${DOCKER_CONTAINERS[@]}"
sleep 10
docker ps

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
docker-compose -f ../catapult-bootstrap/cmds/docker/docker-compose.yml -f ${KILL_COMPOSE_FILE} down --remove-orphans