#!/bin/bash

# Can not be run from gradle, recursive problem?

#Get Project Directory
DIR="$( cd "$( dirname "$0" )" && pwd )"

podman run --volume $DIR/:/root/ -i -t almlarpm /bin/sh -c ./gradlew distTar createModular
cd build/distributions
mv java-runtime/bin/java java-runtime/bin/image-proxy
tar tar -czvf java-runtime.tgz java-runtime

