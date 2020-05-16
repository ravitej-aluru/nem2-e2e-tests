#!/bin/bash

# https://stackoverflow.com/questions/11176284/time-condition-loop-in-shell
# The variable STOP_TIME will store the seconds since 1970-01-01 00:00:00
# UTC, according to the date specified by -d "$2".

# $ ./xxxxxx.sh ... "1 hour 4 minutes 3 seconds" ...
# $ ./xxxxxx.sh ... "tomorrow 8:00am" ...
# $ ./xxxxxx.sh ... "monday 8:00am" ...

# set -e to exit if the time provided by argument 2 is not valid for date.
set -e
STOP_TIME=$1
set +e
SLEEP_INTERVAL=$2
DOCKER_CONTAINERS=(${@:3})
echo "List of Symbol Docker containers to be killed: ${DOCKER_CONTAINERS[@]}"

START_TIME_EPOCH_SECONDS=$(date "+%s")
STOP_TIME_EPOCH_SECONDS=$(date -d "$STOP_TIME" "+%s")

echo -e "\nStart time: $(date)"
echo -e "End time: $(date -d "$STOP_TIME")\n"

# Repeat the loop while the current date is less than STOP_TIME_EPOCH_SECONDS
while [ $(date "+%s") -lt ${STOP_TIME_EPOCH_SECONDS} ]; do
  sleep $SLEEP_INTERVAL
  for container in "${DOCKER_CONTAINERS[@]}"; do
    # Remove the single quotes from the container name string (it was returned by python with '')
    container=$(sed s/\'//g <<<$container)
    echo "$(date)::Processing docker container: $container"
    NODE_STATUS=$(docker inspect $container --format='{{.State.Status}}')
    NODE_EXITCODE=$(docker inspect $container --format='{{.State.ExitCode}}')
    echo "$(date)::container $container status: ${NODE_STATUS}; exitcode: ${NODE_EXITCODE}"
    if [ ${NODE_STATUS} == "exited"  ] && [ ${NODE_EXITCODE} == "137" ]; then
      echo "$(date)::$container was randomly killed by pumba, restarting it."
      docker start $container
    fi
  done
done
