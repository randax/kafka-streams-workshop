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
		// Exercise 3: Join books with count of up-votes
		// Input 0: Book projections (key: bookId, value: BookProjection)
		// Input 1: Up-votes (key: bookId, value: UpVote)
		// Output: Stream of BookProjection records, with up-vote count (key: bookId, value: BookProjection)
//		throw new RuntimeException("Not implemented yet!");
		return null;
	}

}
