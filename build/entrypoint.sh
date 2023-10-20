#!/bin/bash

cd /app

chown -R root:root /app
ls -al /app/udfs/hive/target/
# Build Yauaa
mvn -Dmaven.test.skip=true -Drat.ignoreErrors=true -pl :yauaa-hive -am clean package && \
    cp /app/udfs/hive/target/yauaa-hive-*.jar /target/