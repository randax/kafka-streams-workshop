package no.booster.ex2;

import no.booster.avro.Author;
import no.booster.avro.Book;
import no.booster.avro.BookProjection;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.BiFunction;

@Configuration
public class JoinBookWithAuthor {

	@Bean
	public BiFunction<KTable<String, Book>, KTable<String, Author>, KStream<String, BookProjection>> joinAuthor() {
		// todo Exercise 2
		throw new RuntimeException("Not implemented yet!");
	}
}
