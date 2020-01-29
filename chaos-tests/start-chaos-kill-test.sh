#!/bin/bash

# set -e to exit if the time provided by argument 1 is not valid for date.
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

# CHAOS_LOG_FILE=chaos-logs/$KILL_COMPOSE_FILE.$(date +"%d.%m.%Y-%H.%M.%S").log
# Launch the catapult server and pumba containers
docker-compose -f ../catapult-bootstrap/cmds/docker/docker-compose.yml -f ${KILL_COMPOSE_FILE} up -d
docker ps
docker ps --filter NAME=docker_peer-node-0_1 --filter NAME=docker_peer-node-1_1

# Repeat the loop while the current date is less than STOP_TIME_EPOCH_SECONDS
while [ $(date "+%s") -lt ${STOP_TIME_EPOCH_SECONDS} ]; do
  sleep 60
  docker ps --filter NAME=docker_peer-node-0_1 --filter NAME=docker_peer-node-1_1

  NODE_0_STATUS=$(docker inspect docker_peer-node-0_1 --format='{{.State.Status}}')
  NODE_0_EXITCODE=$(docker inspect docker_peer-node-1_1 --format='{{.State.ExitCode}}')

  if [ ${NODE_0_STATUS} == "exited"  ] && [ ${NODE_0_EXITCODE} == "137" ]; then
    echo "peer-node-0 was randomly killed by pumba, restarting it."
    docker start docker_peer-node-0_1
  fi

  NODE_1_STATUS=$(docker inspect docker_peer-node-1_1 --format='{{.State.Status}}')
  NODE_1_EXITCODE=$(docker inspect docker_peer-node-1_1 --format='{{.State.ExitCode}}')

  if [ ${NODE_1_STATUS} == "exited" ] && [ ${NODE_1_EXITCODE} == "137" ]; then
    echo "peer-node-1 was randomly killed by pumba, restarting it."
    docker start docker_peer-node-1_1
  fi
done

docker-compose -f ../catapult-bootstrap/cmds/docker/docker-compose.yml -f ${KILL_COMPOSE_FILE} down --remove-orphans