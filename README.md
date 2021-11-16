## Getting started

Start services:

    docker-compose up -d

Register debezium connector:

    POST localhost:8083/connectors
    Content-Type: application/json

    {
      "name": "foo",
      "config": {
        "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
        "plugin.name": "pgoutput",
        "database.hostname": "db",
        "database.port": "5432",
        "database.user": "foo",
        "database.password": "foo",
        "database.dbname" : "foo",
        "database.server.name": "local",
        "table.exclude.list": ""
      }
    }

Validate that records are pushed to Kafka topic ``local.public.recipes``

    http://localhost:9000
