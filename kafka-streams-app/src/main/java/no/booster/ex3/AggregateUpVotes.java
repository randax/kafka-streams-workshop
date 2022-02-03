package no.booster.ex3;

import no.booster.avro.BookProjection;
import no.booster.avro.UpVote;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.BiFunction;

@Configuration
public class AggregateUpVotes {

	@Bean
	public BiFunction<KTable<String, BookProjection>, KStream<String, UpVote>, KStream<String, BookProjection>> upVotes() {
		return (books, upVotes) -> books
				.leftJoin(upVotes.groupByKey().count(), this::withKarma)
				.toStream();
	}

	private BookProjection withKarma(BookProjection book, Long karma) {
		return BookProjection.newBuilder(book)
				.setUpVotes(karma)
				.build();
	}
}
