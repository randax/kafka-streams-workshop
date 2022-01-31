package no.booster;

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

public class Ex2JoinBookWithAuthorTest {

    private static final String SCHEMA_REGISTRY_SCOPE = "test";

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, Book> books;
    private TestInputTopic<String, Author> authors;
    private TestOutputTopic<String, BookProjection> bookProjections;

    @Test
    public void testBookHasAuthor() {
        books.pipeInput("0-7679-0817-1", Book.newBuilder()
                .setAuthorId(1L)
                .setTitle("A Short History of Nearly Everything")
                .setDescription("A popular science book that explains some areas of science, using easily accessible language that appeals more to the general public than many other books dedicated to the subject.")
                .build()
        );
        authors.pipeInput("1", Author.newBuilder()
                .setName("Bill Bryson")
                .build()
        );
        assertThat(bookProjections.readValue()).isEqualTo(BookProjection.newBuilder()
                .setTitle("A Short History of Nearly Everything")
                .setAuthor("Bill Bryson")
                .setDescription("A popular science book that explains some areas of science, using easily accessible language that appeals more to the general public than many other books dedicated to the subject.")
                .build()
        );
        assertThat(bookProjections.isEmpty()).isTrue();
    }

    @BeforeEach
    private void beforeEach() {

        final StreamsBuilder builder = new StreamsBuilder();

        final String INPUT_TOPIC_0 = "input-0";
        final String INPUT_TOPIC_1 = "input-1";
        final String OUTPUT_TOPIC = "output";
        final String MOCK_SCHEMA_REGISTRY_URL = "mock://" + SCHEMA_REGISTRY_SCOPE;

        final Serde<String> stringSerde = Serdes.String();
        final Serde<Book> input0Serde = new SpecificAvroSerde<>();
        final Serde<Author> input1Serde = new SpecificAvroSerde<>();
        final Serde<BookProjection> outputSerde = new SpecificAvroSerde<>();

        final Ex2JoinBookWithAuthor app = new Ex2JoinBookWithAuthor();
        final KTable<String, Book> input0 = builder.table(INPUT_TOPIC_0, Consumed.with(stringSerde, input0Serde));
        final KTable<String, Author> input1 = builder.table(INPUT_TOPIC_1, Consumed.with(stringSerde, input1Serde));
        final BiFunction<KTable<String, Book>, KTable<String, Author>, KStream<String, BookProjection>> process = app.joinAuthor();
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
        books = testDriver.createInputTopic(INPUT_TOPIC_0, stringSerde.serializer(), input0Serde.serializer());
        authors = testDriver.createInputTopic(INPUT_TOPIC_1, stringSerde.serializer(), input1Serde.serializer());
        bookProjections = testDriver.createOutputTopic(OUTPUT_TOPIC, stringSerde.deserializer(), outputSerde.deserializer());
    }

    @AfterEach
    void afterEach() {
        testDriver.close();
        MockSchemaRegistry.dropScope(SCHEMA_REGISTRY_SCOPE);
    }
}
