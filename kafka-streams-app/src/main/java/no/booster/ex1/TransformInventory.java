package no.booster.ex1;

import no.booster.avro.Book;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class TransformInventory {

	@Bean
	public Function<
			KStream<no.booster.inventory.book.Key, no.booster.inventory.book.Envelope>,
			KStream<String, Book>> transformBook() {
		return books -> books.map((k, v) -> new KeyValue<>(k.getIsbn().toString(), Book.newBuilder()
				.setTitle(v.getAfter().getTitle())
				.setDescription(v.getAfter().getDescription())
				.setAuthorId(v.getAfter().getAuthorId())
				.build())
		);
	}
}
