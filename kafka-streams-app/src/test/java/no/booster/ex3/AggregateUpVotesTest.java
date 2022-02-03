package no.booster.ex3;

import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import no.booster.avro.BookProjection;
import no.booster.avro.UpVote;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class AggregateUpVotesTest {

	private static final String SCHEMA_REGISTRY_SCOPE = "test";

	private TopologyTestDriver testDriver;

	private TestInputTopic<String, BookProjection> booksIn;
	private TestInputTopic<String, UpVote> upVotes;
	private TestOutputTopic<String, BookProjection> booksOut;

	@Test
	public void testBookWithoutUpVote() {

		String isbn = "0-7679-0817-1";
		BookProjection book = book(isbn);

		booksIn.pipeInput(isbn, book);
		List<KeyValue<String, BookProjection>> res = booksOut.readKeyValuesToList();

		assertThat(res).hasSize(1);
		assertThat(res.get(0).key).isEqualTo(isbn);
		assertThat(res.get(0).value).isEqualTo(book);
	}

	@Test
	public void testBookTwoUpVotes() {

		String isbn = "0-7679-0817-1";
		BookProjection book = book(isbn);

		booksIn.pipeInput(isbn, book);
		upVotes.pipeInput(isbn, UpVote.newBuilder().setUserId(1L).build());
		upVotes.pipeInput(isbn, UpVote.newBuilder().setUserId(2L).build());

		List<BookProjection> res = booksOut.readValuesToList();

		assertThat(res).hasSize(3);
		assertThat(res.get(0)).isEqualTo(book);
		assertThat(res.get(1)).isEqualTo(BookProjection.newBuilder(book).setUpVotes(1L).build());
		assertThat(res.get(2)).isEqualTo(BookProjection.newBuilder(book).setUpVotes(2L).build());
	}

	@Test
	@Disabled("Bonus exercise")
	public void testBookTwoUpVotesFromSameUser() {

		String isbn = "0-7679-0817-1";
		BookProjection book = book(isbn);

		booksIn.pipeInput(isbn, book);
		upVotes.pipeInput(isbn, UpVote.newBuilder().setUserId(1L).build());
		upVotes.pipeInput(isbn, UpVote.newBuilder().setUserId(1L).build());

		List<BookProjection> res = booksOut.readValuesToList();

		assertThat(res).hasSize(3);
		assertThat(res.get(0)).isEqualTo(book);
		assertThat(res.get(1)).isEqualTo(BookProjection.newBuilder(book).setUpVotes(1L).build());
		assertThat(res.get(2)).isEqualTo(BookProjection.newBuilder(book).setUpVotes(1L).build());
	}


	private BookProjection book(String isbn) {
		return BookProjection.newBuilder()
				.setIsbn(isbn)
				.setTitle("A Short History of Nearly Everything")
				.build();
	}

	@BeforeEach
	private void beforeEach() {

		final StreamsBuilder builder = new StreamsBuilder();

		final String INPUT_TOPIC_0 = "input-0";
		final String INPUT_TOPIC_1 = "input-1";
		final String OUTPUT_TOPIC = "output";
		final String MOCK_SCHEMA_REGISTRY_URL = "mock://" + SCHEMA_REGISTRY_SCOPE;

		final Serde<String> stringSerde = Serdes.String();
		final Serde<BookProjection> input0Serde = new SpecificAvroSerde<>();
		final Serde<UpVote> input1Serde = new SpecificAvroSerde<>();
		final Serde<BookProjection> outputSerde = new SpecificAvroSerde<>();

		final AggregateUpVotes app = new AggregateUpVotes();
		final KTable<String, BookProjection> input0 = builder.table(INPUT_TOPIC_0, Consumed.with(stringSerde, input0Serde));
		final KStream<String, UpVote> input1 = builder.stream(INPUT_TOPIC_1, Consumed.with(stringSerde, input1Serde));
		final BiFunction<KTable<String, BookProjection>, KStream<String, UpVote>, KStream<String, BookProjection>> process = app.upVotes();
		final KStream<String, BookProjection> output = process.apply(input0, input1);
		output.to(OUTPUT_TOPIC, Produced.with(stringSerde, outputSerde));

		final Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "TopologyTestDriver");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
		props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL);
		testDriver = new TopologyTestDriver(builder.build(), props);

		// Configure Serdes to use the same mock schema registry URL
		Map<String, String> config = Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, MOCK_SCHEMA_REGISTRY_URL);
		input0Serde.configure(config, false);
		input1Serde.configure(config, false);
		outputSerde.configure(config, false);

		// Define input and output topics to use in tests
		booksIn = testDriver.createInputTopic(INPUT_TOPIC_0, stringSerde.serializer(), input0Serde.serializer());
		upVotes = testDriver.createInputTopic(INPUT_TOPIC_1, stringSerde.serializer(), input1Serde.serializer());
		booksOut = testDriver.createOutputTopic(OUTPUT_TOPIC, stringSerde.deserializer(), outputSerde.deserializer());
	}

	@AfterEach
	void afterEach() {
		testDriver.close();
		MockSchemaRegistry.dropScope(SCHEMA_REGISTRY_SCOPE);
	}
}
