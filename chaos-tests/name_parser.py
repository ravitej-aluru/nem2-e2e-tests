#!/bin/python

import yaml
import os
import sys
import logging


def get_docker_container_names(compose_file):
    with open(compose_file) as file:
        # The FullLoader parameter handles the conversion from YAML
        # scalar values to Python the dictionary format
        compose_yaml = yaml.load(file, Loader=yaml.FullLoader)
    services = compose_yaml['services']
    docker_containers = [v['command'].split(' ')[-1] for v in services.values()]
    print(docker_containers)
    return docker_containers


def get_relative_file_path(file_name):
    logging.debug("Looking for file {}", file_name)
    relative_file_path = [os.path.join(dirpath, filename) for dirpath, _, filenames in os.walk('.') for filename in filenames if filename.endswith(file_name)]
    if len(relative_file_path) > 1:
        logging.warning("More than one docker-compose file with the same name found in sub-directories %s. Returning the first one.", relative_file_path)
        return relative_file_path[0]
    elif len(relative_file_path) == 0:
        logging.error("docker-compose file '%s' not found!", file_name)
    else:
        return relative_file_path[0]


if __name__ == "__main__":
    compose_file = sys.argv[1]
    logging.debug("docker-compose file name: {}", compose_file)
    file_path = get_relative_file_path(compose_file)
    logging.debug("Path of the compose file: {}", file_path)
    get_docker_container_names(file_path)