FROM confluentinc/cp-kafka-connect-base:6.2.1

RUN confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:5.5.6

RUN confluent-hub install --no-prompt debezium/debezium-connector-postgresql:1.7.0
