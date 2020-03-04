#!/bin/python

from pymongo import MongoClient
import logging
import sys


client = None
db = None


def connect_mongo_db(host='localhost', port=27017):
    global client
    global db
    client = MongoClient(host, port)
    print(client.list_database_names())
    db = client.catapult
    print(db.list_collection_names())


def count_transactions():
    print("Count of transactions: ", db.transactions.count_documents({}))


if __name__ == "__main__":
    connect_mongo_db()
    count_transactions()
    # compose_file = sys.argv[1]
    # logging.debug("docker-compose file name: {}", compose_file)
    # file_path = get_relative_file_path(compose_file)
    # logging.debug("Path of the compose file: {}", file_path)
    # get_docker_container_names(file_path)