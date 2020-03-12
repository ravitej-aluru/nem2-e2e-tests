#!/bin/python

import yaml
import os
import sys
import logging
import fire
from pyjavaproperties import Properties

def update_properties_file(properties_file, new_properties):
    p = Properties()
    p.load(open(properties_file))
    # for name, value in [('ENABLEPRINTER', 'y'), ('PRINTERLIST', 'PRNT3')]:
    for name, value in new_properties:
        p[name] = value
    p.store(open(properties_file, 'w'))


def avoid_banning(symbol_server_dir):
    nodes_props_files = [
        os.path.join(dirpath, filename) 
        for dirpath, _, filenames
        in os.walk(os.path.join('..', symbol_server_dir, 'build'))
        for filename in filenames
        if filename is 'config-node.properties']
    for node_props_file in nodes_props_files:
        update_properties_file(
            node_props_file, 
            [('trustedHosts', ''),
             ('localNetworks', '')
             ])


def get_docker_container_names(compose_file):
    with open(compose_file) as file:
        # The FullLoader parameter handles the conversion from YAML
        # scalar values to Python the dictionary format
        compose_yaml = yaml.load(file, Loader=yaml.FullLoader)
    services = compose_yaml['services']
    docker_containers = [v['command'].split(' ')[-1] for v in services.values()]
    # print(docker_containers)
    return docker_containers


def get_relative_file_path(file_name):
    logging.debug('Looking for file %s', file_name)
    relative_file_path = [os.path.join(dirpath, filename) for dirpath, _, filenames in os.walk('..') for filename in filenames if filename.endswith(file_name)]
    if len(relative_file_path) > 1:
        logging.warning("More than one docker-compose file with the same name found in sub-directories %s. Returning the first one.", relative_file_path)
        # print(relative_file_path[0])
        return relative_file_path[0]
    elif len(relative_file_path) == 0:
        logging.error("docker-compose file '%s' not found!", file_name)
    else:
        # print(relative_file_path[0])
        return relative_file_path[0]


def get_first_user_private_key():
    addresses_file = get_relative_file_path('addresses.yaml')
    logging.info('Path of addresses.yml is: {}'.format(addresses_file))
    with open(addresses_file) as file:
        # The FullLoader parameter handles the conversion from YAML
        # scalar values to Python the dictionary format
        addresses_yaml = yaml.load(file, Loader=yaml.FullLoader)
    # print(addresses_yaml['nemesis_addresses'][0]['private'])
    first_address_private_key = addresses_yaml['nemesis_addresses'][0]['private']
    return first_address_private_key


if __name__ == "__main__":
    fire.Fire()
    # avoid_banning('catapult-service-bootstrap')
    # compose_file = sys.argv[1]
    # logging.debug('docker-compose file name: {}'.format(compose_file))
    # file_path = get_relative_file_path(compose_file)
    # logging.debug('Path of the compose file: {}'.format(file_path))
    # get_docker_container_names(file_path)