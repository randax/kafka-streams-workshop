## Before we start

### Add ``dockerhost`` to `/etc/hosts`

This tutorial will refer to ``dockerhost`` as the hostname of your docker VM. It might be a good idea to alias it in
your ``/etc/hosts``
file. If you are on linux, or use Docker for Mac and are used to localhost, then simply add:

```
➜  tail -n1 /etc/hosts
127.0.0.1 dockerhost
```

## Exercise 1

In this exercise, we will learn how to use _Kafka Connect_ with the _Debezium_ plugin to pull data from our Postgres
database into Kafka topics. Once data is available in Kafka topics, we will implement a simple transformation with
_Kafka Streams_, and use another Kafka Connect plugin to load the data into _Elasticsearch_.

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
If this task seems daunting, you will be happy to know that [
Debezium](https://debezium.io/documentation/reference/stable/connectors/postgresql.html)
provides all of that for us. FYI, this process is sometimes referred to as _Change Data Capture_.

But first, let's start up _Kafka Connect_ (with the Debezium plugin installed), and _Kafdrop_ - a GUI to allow us to
easily see the content of our topics.

    docker-compose up -d kafka-connect kafdrop

Open [Kafdrop](http://dockerhost:9000/) to see what topics are created.

Now, register the debezium connector:

    curl -X POST -H 'Content-Type: application/json' -d @./connectors/debezium-source-inventory.json http://dockerhost:8085/connectors

Check the status of the connector:

    curl http://dockerhost:8085/connectors/debezium-source-inventory/status

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

Refresh Kafdrop, and validate that two new topics (_no.booster.inventory.book_ and _no.booster.inventory.author_)
have been created, and that the data from the database tables are imported.

Congratulations, you now have everything set up to sync data from the database to Kafka! And just to see that it works,
let's try to change the title of one of the books in the database:

    update book set title='A Short History of Nearly Everything!' where isbn='0-7679-0817-1';

### Transform data

It is time to write our first _Kafka Stream_ application. This time it even involves writing a bit of Java code.

Open the folder ``kafka-streams-app`` in your favourite IDE, and add the missing parts
in ``no.booster.ex1.TransformInventory``.

Open a new terminal, and ``cd`` into the directory of the kafka streams app. Then create the jar file by running the
following command:

    ./mvnw clean package -Dtest=TransformInventoryTest

From the terminal that you run ``docker-compose``, build and run your first version of the ``kafka-streams-app``:

    docker-compose up -d --build kafka-streams-app

In Kafdrop, you should now see the topic ``books-v1``, and it should contain the records that were produced by your
function ``transformBook``.

### Export data to Elasticsearch

It may be time to ask yourself the question, why am I doing all of this? If you didn't know, your colleague Cecilie has
already built the first version of the new audiobooks search frontend app. Start it up:

    docker-compose up -d audiobooks-search

Cecilie have chosen _Elasticsearch_ because she believes that it can deliver what she needs from the search backend, and
she has even prepared some index mappings to use. Open the app [audiobooks-search](http://dockerhost:3001) that she has
created.

Notice something? No books, right?

Let us help Cecilie with populating the Elasticsearch index. Register an Elasticsearch Sink connector:

    curl -X POST -H 'Content-Type: application/json' -d @./connectors/elasticsearch-sink-books.json http://dockerhost:8085/connectors

Try the search app again, and feel the joy. It is time to ask Cecilie to buy you a coffee!

## Exercise 2

In the first exercise we did set up a lot of stuff, but we didn't really provide a lot of value other than synchronize
data from a Postgresql database to Elasticsearch. It is time to change that!

Cecilie was really happy that the search engine now provides results, but she did note one thing: none of the books
contain any author. And searching by author does not work, as it should. That will be the task of this second exercise.

### Kafka Streams: Join Books with Author

It is time to write more code. This time, it involves a _stateful_
application. Fill in the missing parts in ``no.booster.ex2.JoinBookWithAuthor``.

When the following test runs without exceptions, you can move on

    ./mvnw clean package -Dtest=JoinBookWithAuthorTest

Before you start up the improved version of your kafka streams app, let's change a few settings
in ``docker-compose.yaml``. Update the following environment variables:

```
  audiobooks-search:
    environment:
      BOOKS_INDEX: books-v2
      ...

  kafka-streams-app:
    environment:
      SPRING_KAFKA_FUNCTION_DEFINITION: transformBook;transformAuthor;joinAuthor
      ...
```

These changes tell ``audiobooks-search`` to use a new index, and our ``kafka-streams-app`` to enable the `joinAuthor`
function.

Now, lets restart ``audiobooks-search``:

    docker-compose up -d audiobooks-search

And build and run your updated version of the ``kafka-streams-app``:

    docker-compose up -d --build kafka-streams-app

Open the app [audiobooks-search](http://dockerhost:3001) again and see that the author is both listed and searchable!

