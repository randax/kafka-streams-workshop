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

# Welcome to Workshop

Search backend with Kafka Streams and Connect

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

# Exercise 0: Set up data pipeline

<img src="/flow.png" class="" style="width: 100%" />

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
