# syntax=docker/dockerfile:1

FROM almalinux:9.2-minimal

RUN microdnf install rpm-build -y

RUN curl -L -o jdk-21.0.1+12.tar.gz https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.1%2B12/OpenJDK21U-jdk_x64_linux_hotspot_21.0.1_12.tar.gz \
    && tar -xvf ./jdk-21.0.1+12.tar.gz -C /opt/ \
    && rm jdk-21.0.1+12.tar.gz

ENV PATH $PATH:/opt/jdk-21.0.1+12/bin
ENV JAVA_HOME /opt/jdk-21.0.1+12

WORKDIR /root