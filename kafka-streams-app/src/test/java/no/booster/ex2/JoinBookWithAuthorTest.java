package no.booster.ex2;

import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import no.booster.avro.Author;
import no.booster.avro.Book;
import no.booster.avro.BookProjection;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

public class JoinBookWithAuthorTest {

    private static final String SCHEMA_REGISTRY_SCOPE = "test";

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, Book> booksIn;
    private TestInputTopic<String, Author> authors;
    private TestOutputTopic<String, BookProjection> booksOut;

    @Test
    public void testBookWithoutAuthor() {
        booksIn.pipeInput("0-7679-0817-1", aShortHistoryOfNearlyEverything());
        assertThat(booksOut.isEmpty()).isTrue();
    }

    @Test
    public void testBookHasAuthor() {
        booksIn.pipeInput("0-7679-0817-1", aShortHistoryOfNearlyEverything());
        authors.pipeInput("1", billBryson());
        assertThat(booksOut.readValue()).isEqualTo(book1());
        assertThat(booksOut.isEmpty()).isTrue();
    }

    @Test
    public void testTwoBooksWithAuthor() {
        booksIn.pipeInput("0-7679-0817-1", aShortHistoryOfNearlyEverything());
        booksIn.pipeInput("0-7679-0251-3", aWalkInTheWoods());
        authors.pipeInput("1", billBryson());
        assertThat(booksOut.readValuesToList()).containsExactlyInAnyOrder(book1(), book2());
    }

    private BookProjection book1() {
        return BookProjection.newBuilder()
				.setIsbn("0-7679-0817-1")
                .setTitle("A Short History of Nearly Everything")
                .setAuthor("Bill Bryson")
                .build();
    }

    private BookProjection book2() {
        return BookProjection.newBuilder()
				.setIsbn("0-7679-0251-3")
                .setTitle("A Walk in the Woods: Rediscovering America on the Appalachian Trail")
                .setAuthor("Bill Bryson")
                .build();
    }

    private Author billBryson() {
        return Author.newBuilder()
                .setName("Bill Bryson")
                .build();
    }

    private Book aShortHistoryOfNearlyEverything() {
        return Book.newBuilder()
				.setIsbn("0-7679-0817-1")
                .setAuthorId(1L)
                .setTitle("A Short History of Nearly Everything")
                .build();
    }

    private Book aWalkInTheWoods() {
        return Book.newBuilder()
				.setIsbn("0-7679-0251-3")
                .setAuthorId(1L)
                .setTitle("A Walk in the Woods: Rediscovering America on the Appalachian Trail")
                .build();
    }

    @BeforeEach
    private void beforeEach() {

        final StreamsBuilder builder = new StreamsBuilder();

        final String INPUT_TOPIC_0 = "input-0";
        final String INPUT_TOPIC_1 = "input-1";
        final String OUTPUT_TOPIC = "output";

		final Serde<String> stringSerde = Serdes.String();
        final Serde<Book> input0Serde = new SpecificAvroSerde<>();
        final Serde<Author> input1Serde = new SpecificAvroSerde<>();
        final Serde<BookProjection> outputSerde = new SpecificAvroSerde<>();

        final JoinBookWithAuthor app = new JoinBookWithAuthor();
        final KTable<String, Book> input0 = builder.table(INPUT_TOPIC_0, Consumed.with(stringSerde, input0Serde));
        final KTable<String, Author> input1 = builder.table(INPUT_TOPIC_1, Consumed.with(stringSerde, input1Serde));
        final BiFunction<KTable<String, Book>, KTable<String, Author>, KStream<String, BookProjection>> process = app.joinAuthor();
        final KStream<String, BookProjection> output = process.apply(input0, input1);
        output.to(OUTPUT_TOPIC, Produced.with(stringSerde, outputSerde));

		testDriver = new TopologyTestDriver(builder.build(), new Properties());

        Map<String, String> config = Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "mock://" + SCHEMA_REGISTRY_SCOPE);
        input0Serde.configure(config, false);
        input1Serde.configure(config, false);
        outputSerde.configure(config, false);

        // Define input and output topics to use in tests
        booksIn = testDriver.createInputTopic(INPUT_TOPIC_0, stringSerde.serializer(), input0Serde.serializer());
        authors = testDriver.createInputTopic(INPUT_TOPIC_1, stringSerde.serializer(), input1Serde.serializer());
        booksOut = testDriver.createOutputTopic(OUTPUT_TOPIC, stringSerde.deserializer(), outputSerde.deserializer());
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
        MockSchemaRegistry.dropScope(SCHEMA_REGISTRY_SCOPE);
    }
}
