#!/bin/bash

# https://stackoverflow.com/questions/11176284/time-condition-loop-in-shell
# The variable STOP_TIME will store the seconds since 1970-01-01 00:00:00
# UTC, according to the date specified by -d "$2".

# $ ./xxxxxx.sh ... "1 hour 4 minutes 3 seconds" ...
# $ ./xxxxxx.sh ... "tomorrow 8:00am" ...
# $ ./xxxxxx.sh ... "monday 8:00am" ...

PARAMS=""
while (( "$#" )); do
  case "$1" in
    -a|--api-node)
      if [ -n "$2" ] && [ ${2:0:1} != "-" ]; then
        API_NODE_CONTAINER_NAME=$2
        shift 2
      else
        echo "Error: Argument for $1 is missing" >&2
        exit 1
      fi
      ;;
    -b|--api-broker)
      if [ -n "$2" ] && [ ${2:0:1} != "-" ]; then
        API_BROKER_CONTAINER_NAME=$2
        shift 2
      else
        echo "Error: Argument for $1 is missing" >&2
        exit 1
      fi
      ;;
    -*|--*=) # unsupported flags
      echo "Error: Unsupported flag $1" >&2
      exit 1
      ;;
    *) # preserve positional arguments
      PARAMS="$PARAMS $1"
      shift
      ;;
  esac
done
# set positional arguments in their proper place
eval set -- "$PARAMS"

# set -e to exit if the time provided by argument 2 is not valid for date.
set -e
STOP_TIME=$1
set +e
SLEEP_INTERVAL=$2
DOCKER_CONTAINERS=(${@:3})
DOCKER_CONTAINERS+=("${API_BROKER_CONTAINER_NAME}" "${API_NODE_CONTAINER_NAME}")
echo "$(date)::List of Symbol Docker containers to be monitored: ${DOCKER_CONTAINERS[@]}"

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
    if [[ ${NODE_STATUS} == "exited"  ]] && [[ ${NODE_EXITCODE} == "137" ]]; then
      
      # If it is the api-node-broker, then stop the api-node, start the broker, then start the api-node to avoid failure conditions
      if [[ ${container} == "${API_BROKER_CONTAINER_NAME}" ]]; then
        echo "$(date)::$container was killed by pumba. Stopping $API_NODE_CONTAINER_NAME and restarting both."
        docker stop "${API_NODE_CONTAINER_NAME}"
        sleep 5
        docker start "${API_BROKER_CONTAINER_NAME}"
        sleep 5
        docker start "${API_NODE_CONTAINER_NAME}"
      else
        echo "$(date)::$container was killed by pumba, restarting it."
        docker start "$container"
      fi
    fi
  done
done
