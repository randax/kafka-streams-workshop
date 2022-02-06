## Exercise 1

In this exercise, we will learn how to use _Kafka Connect_ with the _Debezium_ plugin to pull data from our Postgres
database into Kafka topics. Once data is available in Kafka topics, we will implement a simple transformation with _
Kafka Streams_, and use another Kafka Connect plugin to load the data into _Elasticsearch_.

### Inspect database tables

Start up postgres service:

    docker-compose up -d db

In a separate terminal window, run _psql_. Please keep this terminal, as you will later use it to make changes to your
data.

    docker-compose exec db psql -U foo

Books and authors are available under the schema ``inventory``:

    set search_path to inventory;
    select b.isbn, b.title, a.name from book b join author a on a.id=b.author_id;

### Pull data into Kafka topics with Kafka Connect and the Debezium plugin

Now on to the fun part. We will set up an application that pulls data from our database and make it available in Kafka
topics, and what better way than to use the Postgres transaction log as the source!
If this task seems daunting, you will be happy to know that [_
Debezium_](https://debezium.io/documentation/reference/stable/connectors/postgresql.html)
provides all of that for us. FYI, this process is sometimes referred to as _Change Data Capture_.

But first, let's start up _Kafka Connect_ (with the Debezium plugin installed), and _Kafdrop_, a GUI to allow us to
easily see the content of our topics.

    docker-compose up -d kafka-connect kafdrop

Navigate to ``http://docker:9000/`` (replace `docker` with the hostname of your docker virtual machine)
to see what topics are created.

Now, register the debezium connector:

    curl -X POST -H 'Content-Type: application/json' -d @./connectors/debezium-source-inventory.json http://docker:8085/connectors

Check the status of the connector:

    curl http://docker:8085/connectors/debezium-source-inventory/status

Both connector state, and task state should be `RUNNING`:

```
{
  "name": "debezium-source-inventory",
  "connector": {
    "state": "RUNNING",
    "worker_id": "kafka-connect:8085"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "kafka-connect:8085"
    }
  ],
  "type": "source"
}
```

Refresh Kafdrop, anc validate that two new topics (_no.booster.inventory.book_ and _no.booster.inventory.author_)
have been created, and that the data from the database tables are imported.

Congratulations, you now have everything set up to sync data from the database to Kafka! And just to see that it works,
let's try to change the title of one of the books in the database:

    update book set title='A Short History of Nearly Everything!' where isbn='0-7679-0817-1';


