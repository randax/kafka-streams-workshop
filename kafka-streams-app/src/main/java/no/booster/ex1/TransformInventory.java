package no.booster.ex1;

import no.booster.avro.Author;
import no.booster.avro.Book;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class TransformInventory {

	// todo what is "correct" of ktable and kstream here?
	@Bean
	public Function<KStream<no.booster.inventory.book.Key, no.booster.inventory.book.Envelope>, KStream<String, Book>> transformBook() {
		// todo test delete
		return books -> books.map((k, v) -> new KeyValue<>(k.getIsbn().toString(), Book.newBuilder()
				.setIsbn(k.getIsbn())
				.setTitle(v.getAfter().getTitle())
				.setDescription(v.getAfter().getDescription())
				.setAuthorId(v.getAfter().getAuthorId())
				.build())
		);
	}

	@Bean
	public Function<KStream<no.booster.inventory.author.Key, no.booster.inventory.author.Envelope>, KStream<String, Author>> transformAuthor() {
		// todo test delete
		return authors -> authors.map((k, v) -> new KeyValue<>(String.valueOf(k.getId()), Author.newBuilder()
				.setName(v.getAfter().getName())
				.build()));
	}
}
