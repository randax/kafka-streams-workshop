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

	@Bean
	public Function<KStream<no.booster.inventory.book.Key, no.booster.inventory.book.Envelope>, KStream<String, Book>> transformBook() {
		// todo Exercise 1
		throw new RuntimeException("Not implemented yet!");
	}

	@Bean
	public Function<KStream<no.booster.inventory.author.Key, no.booster.inventory.author.Envelope>, KStream<String, Author>> transformAuthor() {
		return authors -> authors.map((k, v) -> new KeyValue<>(String.valueOf(k.getId()), transformAuthor(v)));
	}

	private Author transformAuthor(no.booster.inventory.author.Envelope v) {
		if (v == null || v.getAfter() == null) return null;
		return Author.newBuilder()
				.setName(v.getAfter().getName())
				.build();
	}
}
