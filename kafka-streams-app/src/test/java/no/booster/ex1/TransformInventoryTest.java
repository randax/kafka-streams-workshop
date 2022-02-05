package no.booster.ex1;

import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import no.booster.avro.Book;
import no.booster.inventory.book.Envelope;
import no.booster.inventory.book.Key;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransformInventoryTest {

	private static final String SCHEMA_REGISTRY_SCOPE = "test";

	private TopologyTestDriver testDriver;

	private TestInputTopic<Key, Envelope> inventory;
	private TestOutputTopic<String, Book> books;

	@Test
	public void testInsert() {

		String isbn = "0-7679-0817-1";
		String title = "A Short History of Nearly Everything";
		String description = "A popular science book that explains some areas of science, " +
				"using easily accessible language that appeals more to the general public than many " +
				"other books dedicated to the subject.";
		long authorId = 1L;

		inventory.pipeInput(new Key(isbn), bookCreated(isbn, title, authorId, description));

		KeyValue<String, Book> res = books.readKeyValue();

		assertThat(res.key).isEqualTo(isbn);
		assertThat(res.value).isEqualTo(Book.newBuilder()
				.setIsbn(isbn)
				.setAuthorId(authorId)
				.setTitle(title)
				.setDescription(description)
				.build()
		);
		assertTrue(books.isEmpty());
	}

	@Test
	public void testDelete() {
		String isbn = "0-7679-0817-1";
		inventory.pipeInput(new Key(isbn), null);
		inventory.pipeInput(new Key(isbn), bookDeleted());

		KeyValue<String, Book> b1 = books.readKeyValue();
		assertThat(b1.key).isEqualTo(isbn);
		assertThat(b1.value).isNull();

		KeyValue<String, Book> b2 = books.readKeyValue();
		assertThat(b2.key).isEqualTo(isbn);
		assertThat(b2.value).isNull();

		assertTrue(books.isEmpty());
	}

	private Envelope bookCreated(String isbn, String title, long authorId, String description) {
		return Envelope.newBuilder()
				.setSource(source())
				.setOp("r")
				.setTsMs(1643843496004L)
				.setTransaction(null)
				.setBefore(null)
				.setAfter(bookValue(isbn, title, authorId, description))
				.build();
	}

	private Envelope bookDeleted() {
		return Envelope.newBuilder()
				.setSource(source())
				.setOp("d")
				.setTsMs(1643843496004L)
				.setTransaction(null)
				.setAfter(null)
				.setBefore(bookValue("0-7679-0817-1", "A Short History of Nearly Everything", 1L, ""))
				.build();
	}

	private io.debezium.connector.postgresql.Source source() {
		return io.debezium.connector.postgresql.Source.newBuilder()
				.setVersion("1.7.0.Final")
				.setConnector("postgresql")
				.setName("no.booster")
				.setTsMs(1643843496003L)
				.setSnapshot("true")
				.setDb("audiobooks")
				.setSequence("[null,\"23930744\"]")
				.setSchema$("inventory")
				.setTable("book")
				.setTxId(496L)
				.setLsn(23930744L)
				.setXmin(null)
				.build();
	}

	private no.booster.inventory.book.Value bookValue(String isbn, String title, long authorId, String description) {
		return no.booster.inventory.book.Value.newBuilder()
				.setIsbn(isbn)
				.setTitle(title)
				.setAuthorId(authorId)
				.setDescription(description)
				.build();
	}

	@BeforeEach
	private void beforeEach() {

		final StreamsBuilder builder = new StreamsBuilder();

		final String INPUT_TOPIC = "input";
		final String OUTPUT_TOPIC = "output";

		final Serde<String> stringSerde = Serdes.String();
		final Serde<Key> inputKeySerde = new SpecificAvroSerde<>();
		final Serde<Envelope> inputValueSerde = new SpecificAvroSerde<>();
		final Serde<Book> outputSerde = new SpecificAvroSerde<>();

		final TransformInventory app = new TransformInventory();
		final KStream<Key, Envelope> input = builder.stream(INPUT_TOPIC, Consumed.with(inputKeySerde, inputValueSerde));
		final Function<KStream<Key, Envelope>, KStream<String, Book>> process = app.transformBook();

		final KStream<String, Book> output = process.apply(input);
		output.to(OUTPUT_TOPIC, Produced.with(stringSerde, outputSerde));

		testDriver = new TopologyTestDriver(builder.build(), new Properties());

		Map<String, String> config = Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://" + SCHEMA_REGISTRY_SCOPE);
		inputKeySerde.configure(config, true);
		inputValueSerde.configure(config, false);
		outputSerde.configure(config, false);

		inventory = testDriver.createInputTopic(INPUT_TOPIC, inputKeySerde.serializer(), inputValueSerde.serializer());
		books = testDriver.createOutputTopic(OUTPUT_TOPIC, stringSerde.deserializer(), outputSerde.deserializer());
	}

	@AfterEach
	void afterEach() {
		testDriver.close();
		MockSchemaRegistry.dropScope(SCHEMA_REGISTRY_SCOPE);
	}
}
