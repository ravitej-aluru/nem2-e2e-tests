#!/bin/python

import yaml
import os
import sys


def get_docker_container_names(compose_file):
    with open(compose_file) as file:
        # The FullLoader parameter handles the conversion from YAML
        # scalar values to Python the dictionary format
        compose_yaml = yaml.load(file, Loader=yaml.FullLoader)
    services = compose_yaml['services']
    docker_containers = [v['command'].split(' ')[-1] for v in services.values()]
    print(docker_containers)
    return docker_containers

if __name__ == "__main__":
    kill_compose_file = sys.argv[1]
    print(kill_compose_file)
    kill_file_path = [os.path.join(dirpath, filename) for dirpath, _, filenames in os.walk('.') for filename in filenames if filename.endswith(kill_compose_file)]
    print(kill_file_path)
    get_docker_container_names(kill_file_path[0])