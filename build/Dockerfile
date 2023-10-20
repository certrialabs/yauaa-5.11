FROM ubuntu:22.04

ENV  GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i ~/.ssh/id_rsa"
ARG KEY

RUN apt-get update && apt-get install -y \
    maven \
    git \
    openjdk-8-jdk \
    openjdk-19-jdk \
    openjdk-17-jdk \
    openjdk-11-jdk \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir /root/.m2
COPY build/toolchains.xml /root/.m2/toolchains.xml

RUN mkdir /root/.ssh
RUN echo "$KEY" > /root/.ssh/id_rsa
RUN chmod 600 /root/.ssh/id_rsa

COPY ./ /app
RUN chown -R root:root /app
WORKDIR /app

COPY build/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT [ "/entrypoint.sh" ]
