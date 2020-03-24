# nem2-e2e-tests / chaos-tests

Resilience testing for the NEM Symbol project.

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
        


4. Update the following properties under the ``integrationtests/src/test/resources/configs/config-default.properties`` file to match your bootstrap environment.
    - ``apiServerPublicKey``: Public key of the API server. You can find it in ``build/catapult-config/api-node-0/userconfig/resources/peers-api.json``.
    - ``userPrivateKey``: Private key of the user which will be use to sign each transaction. You can find a list of users in ``build/generated-addresses/addresses.yaml`` file under the ``nemesis_addresses`` section.
    - ``automationPrivateKey``: If automation is running on the same host as the api server(i.e. same IP) then the set to api server boot private key.  Otherwise it can be any private key.
	- ``harvesterPublicKey``: Hasvester's public key of the specified Hasvester in the config-harvesting.properties file.


## Running the tests

1. Open the folder where you have cloned this repository.
2. Move to the ``integrationtests`` folder.

```bash
cd integrationtests
```

3) Build and runt the tests.

```bash
mvn test
```

**Note**: If you have installed an IDE, you cnan run the tests and debug them from there.

## Contributing

Before contributing please [read this](CONTRIBUTING.md).

### Adding new tests

The file structure of the automation tests is as follows:

* Feature files: ``integrationtests/src/test/resources/io/nem``.
* Cucumber steps files: ``integrationtests/src/test/java/io/nem/automation``.

In each of these folders, there is an example folder which has a feature and cucumber steps file respectively.
  
Before adding tests, check the [nem2-scenarios repository](https://github.com/nemtech/nem2-scenarios) for a list of Cucumber feature files already defined. This repository gathers the set of scenarios that should be automated.

To check if a feature file from the nem2 scenarios is already automated, check if the feature file is present in this repository.