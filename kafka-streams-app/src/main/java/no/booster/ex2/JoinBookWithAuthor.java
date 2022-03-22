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
		// Exercise 2: Join books with author by foreign key `author_id`
		// Input 0: Books (key: bookId, value: Book)
		// Input 1: Authors (key: authorId, value: Author)
		// Output: Stream of BookProjection records, with author name from Author (key: bookId, value: BookProjection)
		throw new RuntimeException("Not implemented yet!");
	}
}
