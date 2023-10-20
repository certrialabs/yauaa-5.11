#!/bin/bash


# This script is used to build the Yauaa project.

BUILDER_SSH_KEY=$(cat ../monorepo/dev-secrets/builder-ssh-key)

# build docker image first
if (docker build -t yauaa-build --build-arg KEY="$BUILDER_SSH_KEY" -f build/Dockerfile .);then
    echo "Docker image built successfully"
else
    echo "Docker image build failed"
    exit 1
fi

# run docker image
if (docker run --rm -v "$(pwd)/udfs/hive/target/":/target/ yauaa-build );then
    echo "Build successful"
    echo "JAR is located  in $(pwd)/udfs/hive/target/yauaa-hive-*.jar"
else
    echo "Build failed"
    exit 1
fi