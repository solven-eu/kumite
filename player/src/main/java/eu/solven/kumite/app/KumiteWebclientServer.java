package eu.solven.kumite.app;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
	public Flux<GameMetadata> searchGames() {
		return webClient.get().uri("/api/games").exchangeToFlux(r -> {
			return r.bodyToFlux(GameMetadata.class);
		});
	}

	@Override
	public Flux<GameMetadata> searchContests() {
		return webClient.get().uri("/api/contests").exchangeToFlux(r -> {
			return r.bodyToFlux(GameMetadata.class);
		});
	}

	@Override
	public Mono<ContestView> loadBoard() {
		return webClient.get().uri("/api/board").exchangeToMono(r -> {
			return r.bodyToMono(ContestView.class);
		});
	}

}
