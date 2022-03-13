#!/bin/bash

docker pull debezium/postgres:13
docker pull node:16
docker pull openjdk:17.0.1-slim-buster
docker pull confluentinc/cp-kafka:5.5.6
docker pull confluentinc/cp-zookeeper:5.5.6
docker pull confluentinc/cp-schema-registry:5.5.6
docker pull confluentinc/cp-kafka-connect-base:6.2.1
docker pull docker.elastic.co/elasticsearch/elasticsearch:7.14.2
docker pull docker.elastic.co/kibana/kibana:7.14.2
docker pull obsidiandynamics/kafdrop:3.27.0

cd kafka-streams-app && ./mvnw clean package -DskipTests=true
