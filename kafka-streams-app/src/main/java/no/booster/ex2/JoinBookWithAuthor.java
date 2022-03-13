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
        return (books, authors) -> books
                .join(authors, this::byAuthorId, this::merge)
                .toStream();
    }

    private BookProjection merge(Book book, Author author) {
        return BookProjection.newBuilder()
				.setIsbn(book.getIsbn())
                .setTitle(book.getTitle())
                .setDescription(book.getDescription())
				.setThumbnail(book.getThumbnail())
                .setAuthor(author.getName())
                .build();
    }

    private String byAuthorId(Book t) {
        return String.valueOf(t.getAuthorId());
    }
}
