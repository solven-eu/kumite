package eu.solven.kumite.app.server;

import java.util.Map;
import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import eu.solven.kumite.contest.ContestMetadataRaw;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.leaderboard.LeaderBoardRaw;
import eu.solven.kumite.player.PlayerContestStatus;
import eu.solven.kumite.player.PlayerRawMovesHolder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * See routes as defined in KumiteRouter
 * 
 * @author Benoit Lacelle
 *
 */
// https://www.baeldung.com/spring-5-webclient
@Slf4j
public class KumiteWebclientServer implements IKumiteServer {
	WebClient webClient;

	public KumiteWebclientServer(Environment env) {
		String serverUrl = env.getRequiredProperty("kumite.server.base-url");
		String accessToken = env.getRequiredProperty("kumite.server.access_token");

		webClient = WebClient.builder()
				.baseUrl(serverUrl)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.build();
	}

	// https://github.com/spring-projects/spring-boot/issues/5077
	public KumiteWebclientServer(Environment env, int randomServerPort) {
		String serverUrl = env.getRequiredProperty("kumite.server.base-url")
				.replaceFirst("LocalServerPort", Integer.toString(randomServerPort));
		String accessToken = env.getRequiredProperty("kumite.server.access_token");

		webClient = WebClient.builder()
				.baseUrl(serverUrl)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.build();
	}

	@Override
	public Flux<GameMetadata> searchGames(GameSearchParameters search) {
		RequestHeadersSpec<?> spec = webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/api/games")
						.queryParamIfPresent("game_id", search.getGameId())
						.queryParamIfPresent("title_regex", search.getTitleRegex())
						.build());

		return spec.exchangeToFlux(r -> {
			if (!r.statusCode().is2xxSuccessful()) {
				throw new IllegalArgumentException("Request rejected: " + r.statusCode());
			}
			log.info("Search for games: {}", r.statusCode());
			return r.bodyToFlux(GameMetadata.class);
		});
	}

	@Override
	public Flux<ContestMetadataRaw> searchContests(ContestSearchParameters search) {
		RequestHeadersSpec<?> spec = webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/api/contests")
						.queryParamIfPresent("game_id", search.getGameId())
						.queryParamIfPresent("contest_id", search.getContestId())
						.build());

		return spec.exchangeToFlux(r -> {
			if (!r.statusCode().is2xxSuccessful()) {
				throw new IllegalArgumentException("Request rejected: " + r.statusCode());
			}
			log.info("Search for contests: {}", r.statusCode());
			return r.bodyToFlux(ContestMetadataRaw.class);
		});
	}

	@Override
	public Mono<ContestView> loadBoard(UUID playerId, UUID contestId) {
		RequestHeadersSpec<?> spec = webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/api/board")
						.queryParam("player_id", playerId)
						.queryParam("contest_id", contestId)
						.build());

		return spec.exchangeToMono(r -> {
			if (!r.statusCode().is2xxSuccessful()) {
				throw new IllegalArgumentException("Request rejected: " + r.statusCode());
			}
			return r.bodyToMono(ContestView.class);
		});
	}

	@Override
	public Mono<PlayerContestStatus> joinContest(UUID playerId, UUID contestId) {
		RequestBodySpec spec = webClient.post()
				.uri(uriBuilder -> uriBuilder.path("/api/board/player")
						.queryParam("player_id", playerId)
						.queryParam("contest_id", contestId)
						.build());

		return spec.bodyValue(contestId).exchangeToMono(r -> {
			if (!r.statusCode().is2xxSuccessful()) {
				throw new IllegalArgumentException("Request rejected: " + r.statusCode());
			}
			return r.bodyToMono(PlayerContestStatus.class);
		});
	}

	@Override
	public Mono<PlayerRawMovesHolder> getExampleMoves(UUID playerId, UUID contestId) {
		RequestHeadersSpec<?> spec = webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/api/board/moves")
						.queryParam("player_id", playerId)
						.queryParam("contest_id", contestId)
						.build());

		return spec.exchangeToMono(r -> {
			if (!r.statusCode().is2xxSuccessful()) {
				throw new IllegalArgumentException("Request rejected: " + r.statusCode());
			}
			log.info("Search for moves: {}", r.statusCode());
			return r.bodyToMono(PlayerRawMovesHolder.class);
		});
	}

	@Override
	public Mono<ContestView> playMove(UUID playerId, UUID contestId, Map<String, ?> move) {
		RequestBodySpec spec = webClient.post()
				.uri(uriBuilder -> uriBuilder.path("/api/board/move")
						.queryParam("player_id", playerId)
						.queryParam("contest_id", contestId)
						.build());

		return spec.bodyValue(move).exchangeToMono(r -> {
			if (!r.statusCode().is2xxSuccessful()) {
				throw new IllegalArgumentException("Request rejected: " + r.statusCode());
			}
			return r.bodyToMono(ContestView.class);
		});
	}

	@Override
	public Mono<LeaderBoardRaw> loadLeaderboard(UUID contestId) {
		RequestHeadersSpec<?> spec = webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/api/leaderboards")
						// .queryParam("player_id", playerId)
						.queryParam("contest_id", contestId)
						.build());

		return spec.exchangeToMono(r -> {
			if (!r.statusCode().is2xxSuccessful()) {
				throw new IllegalArgumentException("Request rejected: " + r.statusCode());
			}
			log.info("Search for leaderboard: {}", r.statusCode());
			return r.bodyToMono(LeaderBoardRaw.class);
		});
	}

}