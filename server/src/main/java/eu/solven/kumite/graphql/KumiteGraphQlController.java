package eu.solven.kumite.graphql;

import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GamesRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Look at http://localhost:8080/graphiql
// https://spring.io/guides/gs/graphql-server
// https://www.baeldung.com/spring-graphql
// https://medium.com/dandelion-tutorials/using-spring-webflux-with-graphql-dd2aa381603b
@Controller
public class KumiteGraphQlController {
	GamesRegistry gamesStore;
	ContestsRegistry contestsStore;

	@QueryMapping
	public Flux<GameMetadata> games() {
		return Flux.fromStream(gamesStore.getIdToGame().values().stream().map(g -> g.getGameMetadata()));
	}

	@QueryMapping
	public Mono<GameMetadata> gameById(@Argument UUID gameId) {
		return Mono.just(gamesStore.getGame(gameId).getGameMetadata());
	}

	@SchemaMapping
	public Contest contestById(UUID contestId) {
		return contestsStore.getContest(contestId);
	}
}