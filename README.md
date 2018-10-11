* Introduction

The following project is mimics the behaviour of a distributed resource catalog
that is persistent and accessed via HTTP API, it is dockerized, self managed and deployed.

this project is for evaluation and testing purposes only.


* Quick start - Linux / osX

    * Build
    sh <PROJECT_ROOT>/build.sh

    * Test
    sh <PROJECT_ROOT>/test.sh

    * Run (non persistent)
    sh <PROJECT_ROOT>/run.sh


<PROJECT_ROOT> should be replaced with the full path of the project sources.


* Quick start - Windows
stop wasting your time and move to linux :)


* Persistence
Due to docker's nature of being isolated container, the data will be erased once the container is destroyed.
if you would like to store the data forever please follow this instructions

    * Run
    sh <PROJECT_ROOT>/run.sh <PATH_TO_STORE_DATA>
<PATH_TO_STORE_DATA> should be replaced with the full path of the location to store all the data on the local disk.


* Configuration