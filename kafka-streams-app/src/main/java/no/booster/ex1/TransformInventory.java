package no.booster.ex1;

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
			KStream<String, no.booster.avro.Book>> transformBook() {

		// todo test delete
		return books -> books.map((k, v) -> new KeyValue<>(k.getIsbn().toString(), no.booster.avro.Book.newBuilder()
				.setTitle(v.getAfter().getTitle())
				.setDescription(v.getAfter().getDescription())
				.setAuthorId(v.getAfter().getAuthorId())
				.build())
		);
	}

	@Bean
	public Function<
			KStream<no.booster.inventory.author.Key, no.booster.inventory.author.Envelope>,
			KStream<String, no.booster.avro.Author>> transformAuthor() {

		// todo test delete
		return authors -> authors.map((k, v) -> new KeyValue<>(String.valueOf(k.getId()), no.booster.avro.Author.newBuilder()
				.setName(v.getAfter().getName())
				.build()));
	}
}
