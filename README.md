* Introduction

The following project is mimics the behaviour of a distributed resource catalog
that is persistent and accessed via HTTP API, it is dockerized, self managed and deployed.

this project is for evaluation and testing purposes only.


* Quick start - Linux / osX

    run scripts from <PROJECT_ROOT>.
    where <PROJECT_ROOT> should be replaced with the full path of the project sources.

    * Build
    sh <PROJECT_ROOT>/build.sh

    * Test
    sh <PROJECT_ROOT>/test.sh

    * Run (non persistent)
    sh <PROJECT_ROOT>/run.sh

* Quick start - Windows
stop wasting your time and move to linux :)


* Persistence - Docker
Due to docker's nature of being isolated container, the data will be erased once the container is destroyed.
please set a bridge volume between the docker container path and the host using the following pseudo command

docker run ... -v <local host path>:<path on docker container> ...


* Configuration
    configuration can be overridden using direct manipulation of the conf/application.conf
    or using system properties -D<config entry>=<value>

    theres also a possibility to change configs using env variable but only to selected set of configs:

    ARGUS_DATA_PATH=<valid path on local disk>