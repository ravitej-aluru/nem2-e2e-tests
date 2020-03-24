# nem2-e2e-tests / chaos-tests

Resilience testing for the NEM Symbol project. This is achieved by utilising an open-source tool called [pumba](https://github.com/alexei-led/pumba) for performing resilience testing for dockerised applications.

Scenarios currently available:
1. Randomly kill peer nodes

Scenarios partly completed:
1. Randomly kill mongo db node
2. Slow traffic to/from peer and api nodes
3. Delayed packets to/from peer nodes and mongo db

Scenarios on the roadmap:
1. Randomly kill peer and api nodes
2. Random packet corruption to/from peer nodes
3. Randon packet corruption to/from peer and api nodes
4. Random packet corruption to/from api and db nodes

## Pre-requisites

* [Docker](https://docs.docker.com/install/linux/docker-ce/centos/)
Docker is a pre-requisite for running Symbol bootstrap anyway.

## Installation

1. Clone this repository:

```bash
git clone --recurse-submodules https://github.com/NEMStudios/nem2-e2e-tests.git
```
2. Update the submodules:

```bash
git submodule update --init --recursive
```
3. Edit configs to avoid banning during resilience testing
   1. Navigate to `catapult-service-bootstrap/ruby/catapult-templates`
   2. Edit `api_node/resources/config-node.properties.mt`
   3. Set `localNetworks` and `trustedHosts` values to empty values. Below is a segment of this file showing how it should be after editing.
        ```config
        # all hosts are trusted when list is empty
        trustedHosts =
        localNetworks =
        ```
   4. Do the same in `peer_node/resources/config-node.properties.mt`

## Running the tests

1. Open the folder where you have cloned this repository.
2. Move to the ``chaos-tests`` folder.

```bash
cd chaos-tests
```

3) Run the tests.

```bash
./start-chaos-kill-test.sh 'chaos-kill-peers.yml' '30minutes' '100'
```
The first argument accepts a docker-compose file which is the `pumba` file.

The second argument accepts time in a variety of formats. For example,
`"1 hour 4 minutes 3 seconds"`
`"Monday 8:00am"`
`"Tomorrow 3:00pm"`

The third argument is the transaction rate per second

## Contributing

Before contributing please [read this](CONTRIBUTING.md).