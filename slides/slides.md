---
# try also 'default' to start simple
theme: default
# random image from a curated Unsplash collection by Anthony
# like them? see https://unsplash.com/collections/94734566/slidev
background: https://source.unsplash.com/collection/94734566/1920x1080
# apply any windi css classes to the current slide
class: 'text-center'
# https://sli.dev/custom/highlighters.html
highlighter: shiki
layout: cover
title: Building a search engine backend with Kafka Streams and Connect
colorSchema: dark
# show line numbers in code blocks
lineNumbers: false
# some information about the slides, markdown enabled
info: |
  ## Slidev Starter Template
  Presentation slides for developers.

  Learn more at [Sli.dev](https://sli.dev)
# persist drawings in exports and build
drawings:
  persist: false
---

# Search backend with Kafka Streams and Connect

Booster 2022

<div class="pt-12">
  <span @click="$slidev.nav.next" class="px-2 py-1 rounded cursor-pointer" hover="bg-white bg-opacity-10">
    Let's get started <carbon:arrow-right class="inline"/>
  </span>
</div>

<div class="abs-br m-6 flex gap-2">
  <a href="https://github.com/mlundela/kafka-streams-workshop" target="_blank" alt="GitHub"
    class="text-xl icon-btn opacity-50 !border-none !hover:text-white">
    <carbon-logo-github />
  </a>
</div>

<!--
The last comment block of each slide will be treated as slide notes. It will be visible and editable in Presenter Mode along with the slide. [Read more in the docs](https://sli.dev/guide/syntax.html#notes)
-->
---
layout: intro
---

<h1 text="!5xl">Øyvind Randa</h1>

<div class="leading-8 opacity-80">
Technology enthusiast and Leader for <a href="https://gdgbergen.no/" target="_blank">Google Developer Group Bergen</a>.<br>
Solution Architect at <a href="https://nextgentel.no" target="_blank">NextGenTel</a>.<br>
</div>

<div class="my-10 grid grid-cols-[40px,1fr] w-min gap-y-4">
  <ri-github-line class="opacity-50"/>
  <div><a href="https://github.com/randax" target="_blank">randax</a></div>
  <ri-twitter-line class="opacity-50"/>
  <div><a href="https://twitter.com/oyvir" target="_blank">oyvir</a></div>
</div>

<img src="/images/oyvind.png" class="rounded-full w-40 abs-tr mt-30 mr-20"/>

---
layout: intro
---

<h1 text="!5xl">Mads Lundeland</h1>

<div class="leading-8 opacity-80">
Software developer at <a href="https://tv2.no" target="_blank">TV 2</a>.<br>
Experience with running Kafka Streams and Elasticsearch in production.<br>
</div>

<div class="my-10 grid grid-cols-[40px,1fr] w-min gap-y-4">
  <ri-github-line class="opacity-50"/>
  <div><a href="https://github.com/mlundela" target="_blank">mlundela</a></div>
  <ri-twitter-line class="opacity-50"/>
  <div><a href="https://twitter.com/mlundela" target="_blank">mlundela</a></div>
</div>

<img src="/images/mads.jpeg" class="rounded-full w-40 abs-tr mt-30 mr-20"/>

---
layout: center
---

# Technology Stack


---
layout: center
---

# Practical information
- GitHub: https://github.com/mlundela/kafka-streams-workshop
- Clone repo and run the install scripts.




---
layout: center
---

# Technology Stack

---
name: KafkaStreams
layout: center
---
<div class="grid grid-cols-[3fr,2fr] gap-4">
  <div class="text-center pb-4">
  <img class="h-50 inline-block" src="https://apache.org/logos/res/kafka/kafka_highres.png">
  <div class="opacity-50 mb-2 text-sm">
      Open-source distributed event streaming platform
  </div>
  <div class="text-center">
    <a class="!border-none" href="https://github.com/apache/kafka" target="__blank"><img class="mt-2 h-4 inline mx-0.5" alt="GitHub stars" src="https://img.shields.io/github/stars/apache/kafka?style=social"></a>
   </div>
  </div>
  <div class="border-l border-gray-400 border-opacity-25 !all:leading-12 !all:list-none my-auto">

  - High throughput
  - Scalable
  - Permanent storage
  - High availability
  - Built-in stream processing
  - Connect to almost anything
  </div>
</div>

---
layout: center
class: text-center
---
 # Kafka Streams
 key concepts 

---
layout two-cols
---
# What is Kafka Streams?

- Simple and lightweight client library.
- No external dependencies on systems other than Apache Kafka itself.
- fault-tolerant local state.
- Supports exactly-once processing.
- Employs one-record-at-a-time processing to achieve millisecond processing latency, and supports event-time based windowing operations with out-of-order arrival of records.
- Offers necessary stream processing primitives, along with a high-level Streams DSL and a low-level Processor API.

---
name: Kafka Streams DSL
---
# Kafka Streams DSL
Operators that come out of the box
  - Map
  - Filter
  - Join
  - Aggregations

--- 
name: Kafka Streams and tables
--- 

# Kafka Streams and Tables
  - KStreams
    - All <b>Inserts</b>
    - Similar to a log
  - KTable
    - All <b>upserts</b> on non-null values
    - Similar to a table
    - Typically used with log compacted topics

---
name: Kafka Streams and statestore
layout: two-cols
---
# Kafka Streams State Store
For stateful operations, Kafka Streams uses local state stores that are made fault-tolerant by associated changelog topics stored in Kafka. For these state stores, Kafka Streams uses RocksDB as its default storage to maintain local state on a computing node (think: a container that runs one instance of your distributed application).

<a href="https://www.confluent.io/blog/how-to-tune-rocksdb-kafka-streams-state-stores-performance/" target="_blank">Read more here</a>
::right::

<img src="/images/statedrawing.png" />

---
name: KafkaConnect
layout: center
---
 # Kafka Connect

- Scaleable tool for streaming between Kafka and and other data systems.
- Works as a centralized data hub for simple data integration between databases, key-value stores, and file systems

---
name: Debezium
layout: center
---
# Debezium

- Debezium captures the changes in a transaction log and produces a stream of change events.
- Works as a plugin for Kafka Connect
---
name: Elasticsearch
layout: center
---
 # Elasticsearch

- Elasticsearch is a distributed, RESTful search and analytics engine.
- Often used as backend for web frontend.

---
layout: two-cols
---

# Exercises

0) Set up data pipeline
1) Stateless transform
2) Joins
3) Aggregation

Bonus exercise:

4) Schema evolution

::right::

<img src="/audiobooks-search.png" />


---

# Exercise 0: Data pipeline

<img src="/flow.png" class="" style="width: 100%" />

---

# Exercise 0: Postgres

Set up Postgres audiobooks database

Start up postgres service:

```shell
    docker-compose up -d db
```


In a separate terminal window, run _psql_. Please keep this terminal, as you will later use it to make changes to your
data.

```shell
    docker-compose exec db psql -U admin -d audiobooks
```

Books and authors are available under the schema ``inventory``:

```shell
    set search_path to inventory;
    select b.isbn, b.title, a.name as author from book b join author a on a.id=b.author_id limit 10;
```
---

# Exercise 0: Debezium
Set up Debezium

Start up _Kafka Connect_ (with the Debezium plugin installed), and _Kafdrop_ - a GUI to allow us to
easily see the content of our topics.

```shell
    docker-compose up -d kafka-connect kafdrop
```

Open [Kafdrop](http://dockerhost:9000/) to see what topics are created.

Now, register the debezium connector:

```shell
    curl -X POST -H 'Content-Type: application/json' \
    -d @./kafka-connect/debezium-source-inventory.json \ 
    http://dockerhost:8085/connectors
```

---

# Exercise 1: Stateless transformations

Transform stream of Debezium records

```java {all}
  @Bean
  public Function<KStream<Key, Envelope>, KStream<String, Book>> transformBook() {
      // Input: Stream of records from Debezium
      // Output: Stream of Book records, with bookId as key (key: bookId, value: Book)
      return books -> ...
  }

  // Example: authors
  @Bean
  public Function<KStream<Key, Envelope>, KStream<String, Author>> transformAuthor() {
      return authors -> authors.map((k, v) -> new KeyValue<>(String.valueOf(k.getId()), transformAuthor(v)));
  }
```

Operators that could come in handy:
- _map_


---

# Exercise 1: Unit test

```java {all}

  private TestInputTopic<Key, Envelope> inputTopic;
  private TestOutputTopic<String, Book> outputTopic;

  @Test
  public void testInsert() {

      // Given input
      inputTopic.pipeInput(key(), bookCreated());

      // When output one record
      KeyValue<String, Book> record = outputTopic.readKeyValue();

      assertThat(record.key).isEqualTo("0-7679-0817-1");
      assertThat(record.value.getTitle()).isEqualTo("A Short History of Nearly Everything");
      assertTrue(outputTopic.isEmpty());
  }

```

---

# Exercise 2: Joins

Join books with author by foreign key

```java {all}
  	@Bean
	public BiFunction<KTable<String, Book>, KTable<String, Author>, KStream<String, BookProjection>> joinAuthor() {
		// Input 0: Books (key: bookId, value: Book)
		// Input 1: Authors (key: authorId, value: Author)
		// Output: Stream of BookProjection records, with author name from Author (key: bookId, value: BookProjection)
		return (books, authors) -> ...
	}
```

Operators that could come in handy:
- _join_

---

# Exercise 3: Aggregations

Join books with count of up-votes

```java {all}
	@Bean
	public BiFunction<KTable<String, BookProjection>, KStream<String, UpVote>, KStream<String, BookProjection>> upVotes() {
		// Input 0: Book projections (key: bookId, value: BookProjection)
		// Input 1: Up-votes (key: bookId, value: UpVote)
		// Output: Stream of BookProjection records, with up-vote count (key: bookId, value: BookProjection)
		return (books, upVotes) -> ...
	}

```

Operators that could come in handy:
- _leftJoin_
- _groupByKey_
- _count_
