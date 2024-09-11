package eu.solven.kumite.app;

import java.util.UUID;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import eu.solven.kumite.contest.ContestMetadataRaw;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * See routes as defined in KumiteRouter
 * 
 * @author Benoit Lacelle
 *
 */
// https://www.baeldung.com/spring-5-webclient
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

	@Override
	public Flux<GameMetadata> searchGames(GameSearchParameters search) {
		RequestHeadersSpec<?> spec = webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/api/games")
						.queryParamIfPresent("game_id", search.getGameId())
						.queryParamIfPresent("title_regex", search.getTitleRegex())
						.build());

		return spec.exchangeToFlux(r -> {
			return r.bodyToFlux(GameMetadata.class);
		});
	}

	@Override
	public Flux<ContestMetadataRaw> searchContests(ContestSearchParameters contestSearchParameters) {
		return webClient.get().uri("/api/contests").exchangeToFlux(r -> {
			return r.bodyToFlux(ContestMetadataRaw.class);
		});
	}

	@Override
	public Mono<ContestView> loadBoard(UUID contestId, UUID playerId) {
		return webClient.get().uri("/api/board").exchangeToMono(r -> {
			return r.bodyToMono(ContestView.class);
		});
	}

	@Override
	public Mono<ContestView> joinContest(UUID playerId, UUID contestId) {
		return webClient.post().uri("/api/board/player").bodyValue(contestId).exchangeToMono(r -> {
			return r.bodyToMono(ContestView.class);
		});
	}

}
