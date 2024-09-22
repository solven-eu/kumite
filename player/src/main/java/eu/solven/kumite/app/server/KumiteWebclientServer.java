package eu.solven.kumite.app.server;

import java.text.ParseException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import eu.solven.kumite.app.KumiteWebclientServerProperties;
import eu.solven.kumite.contest.ContestMetadataRaw;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.leaderboard.LeaderboardRaw;
import eu.solven.kumite.login.AccessTokenWrapper;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerContestStatus;
import eu.solven.kumite.player.PlayerRawMovesHolder;
import eu.solven.kumite.player.PlayerSearchParameters;
import lombok.extern.slf4j.Slf4j;
import oshi.util.MemoizedSupplier;
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
	final String PREFIX = "/api/v1";

	final String baseUrl;
	final String refreshToken;

	// Used to interact with playerId-less APIs (like fetching games).
	final UUID defaultPlayerId;

	final AtomicReference<WebClient> webClientRef = new AtomicReference<>();

	final Map<UUID, Supplier<Mono<AccessTokenWrapper>>> playerIdToAccessTokenSupplier = new ConcurrentHashMap<>();

	public static KumiteWebclientServer fromProperties(KumiteWebclientServerProperties properties) {
		return new KumiteWebclientServer(properties.getBaseUrl(), properties.getRefreshToken());
	}

	public KumiteWebclientServer(String baseUrl, String refreshToken) {
		this.baseUrl = baseUrl;
		this.refreshToken = refreshToken;

		NavigableSet<UUID> playerIds = logAboutRefreshToken(refreshToken);
		defaultPlayerId = playerIds.stream()
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("The refresh_token does not give access to any playerId"));
	}

	private NavigableSet<UUID> logAboutRefreshToken(String refreshToken) {
		SignedJWT jws;
		try {
			jws = SignedJWT.parse(refreshToken);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid refreshToken (JWS)", e);
		}

		JWTClaimsSet jwtClaimsSet;
		try {
			jwtClaimsSet = jws.getJWTClaimsSet();
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid refreshToken (claimSet)", e);
		}

		List<String> playerIds;
		try {
			playerIds = jwtClaimsSet.getStringListClaim("playerIds");
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid refreshToken (claimSet.playerIds)", e);
		}

		log.info("refresh_token.jti={} playerIds={}", jwtClaimsSet.getJWTID(), playerIds);

		return playerIds.stream().map(UUID::fromString).sorted().collect(Collectors.toCollection(TreeSet::new));
	}

	WebClient getWebClient() {
		if (webClientRef.get() == null) {
			webClientRef.set(WebClient.builder()
					.baseUrl(baseUrl)
					// TODO Generate accessToken given the refreshToken
					// .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
					.build());
		}

		return webClientRef.get();
	}

	private Mono<AccessTokenWrapper> requestAccessToken(UUID playerId) {
		RequestHeadersSpec<?> spec = getWebClient().get()
				.uri(uriBuilder -> uriBuilder.path(PREFIX + "/oauth2/token")
						.queryParam("player_id", playerId.toString())
						.build())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken);

		return spec.exchangeToMono(r -> {
			if (!r.statusCode().is2xxSuccessful()) {
				throw new IllegalArgumentException("Request rejected: " + r.statusCode());
			}
			log.info("Request for access_token: {}", r.statusCode());
			return r.bodyToMono(AccessTokenWrapper.class);
		});
	}

	// https://stackoverflow.com/questions/65972564/spring-reactive-web-client-rest-request-with-oauth-token-in-case-of-401-response
	Mono<AccessTokenWrapper> accessToken(UUID playerId) {
		// Return the same Mono for 45 minutes
		Function<? super UUID, ? extends Supplier<Mono<AccessTokenWrapper>>> mappingFunction =
				k -> MemoizedSupplier.memoize(() -> requestAccessToken(k), Duration.ofMinutes(45));
		Supplier<Mono<AccessTokenWrapper>> supplier =
				playerIdToAccessTokenSupplier.computeIfAbsent(playerId, mappingFunction);

		return supplier.get()
				// Given Mono will see its value cache for 45 minutes
				.cache(Duration.ofMinutes(45));
	}

	// see GameSearchHandler
	@Override
	public Flux<GameMetadata> searchGames(GameSearchParameters search) {
		return accessToken(defaultPlayerId).map(accessToken -> {
			RequestHeadersSpec<?> spec = getWebClient().get()
					.uri(uriBuilder -> uriBuilder.path(PREFIX + "/games")
							.queryParamIfPresent("game_id", search.getGameId())
							.queryParamIfPresent("title_regex", search.getTitleRegex())
							.queryParam("tag", search.getRequiredTags())
							.queryParamIfPresent("title_regex", search.getTitleRegex())
							.queryParamIfPresent("min_players",
									search.getMinPlayers().stream().mapToObj(Integer::toString).findAny())
							.queryParamIfPresent("max_players",
									search.getMaxPlayers().stream().mapToObj(Integer::toString).findAny())
							.build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken());

			return spec;
		}).flatMapMany(spec -> {
			return spec.exchangeToFlux(r -> {
				if (!r.statusCode().is2xxSuccessful()) {
					throw new IllegalArgumentException("Request rejected: " + r.statusCode());
				}
				log.info("Search for games: {}", r.statusCode());
				return r.bodyToFlux(GameMetadata.class);
			});
		});
	}

	@Override
	public Flux<KumitePlayer> searchPlayers(PlayerSearchParameters search) {
		return accessToken(search.getPlayerId().orElse(defaultPlayerId)).map(accessToken -> {
			RequestHeadersSpec<?> spec = getWebClient().get()
					.uri(uriBuilder -> uriBuilder.path(PREFIX + "/players")
							.queryParamIfPresent("account_id", search.getAccountId())
							.queryParamIfPresent("contest_id", search.getContestId())
							.queryParamIfPresent("player_id", search.getPlayerId())
							.build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken());

			return spec;
		}).flatMapMany(spec -> {
			return spec.exchangeToFlux(r -> {
				if (!r.statusCode().is2xxSuccessful()) {
					throw new IllegalArgumentException("Request rejected: " + r.statusCode());
				}
				log.info("Search for players: {}", r.statusCode());
				return r.bodyToFlux(KumitePlayer.class);
			});
		});
	}

	@Override
	public Flux<ContestMetadataRaw> searchContests(ContestSearchParameters search) {
		return accessToken(defaultPlayerId).map(accessToken -> {
			RequestHeadersSpec<?> spec = getWebClient().get()
					.uri(uriBuilder -> uriBuilder.path(PREFIX + "/contests")
							.queryParamIfPresent("game_id", search.getGameId())
							.queryParamIfPresent("contest_id", search.getContestId())
							.build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken());

			return spec;
		}).flatMapMany(spec -> {
			return spec.exchangeToFlux(r -> {
				if (!r.statusCode().is2xxSuccessful()) {
					throw new IllegalArgumentException("Request rejected: " + r.statusCode());
				}
				log.info("Search for contests: {}", r.statusCode());
				return r.bodyToFlux(ContestMetadataRaw.class);
			});
		});
	}

	@Override
	public Mono<ContestView> loadBoard(UUID playerId, UUID contestId) {
		return accessToken(playerId).map(accessToken -> {
			RequestHeadersSpec<?> spec = getWebClient().get()
					.uri(uriBuilder -> uriBuilder.path(PREFIX + "/board")
							.queryParam("player_id", playerId)
							.queryParam("contest_id", contestId)
							.build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken());

			return spec;
		}).flatMap(spec -> {
			return spec.exchangeToMono(r -> {
				if (!r.statusCode().is2xxSuccessful()) {
					throw new IllegalArgumentException("Request rejected: " + r.statusCode());
				}
				log.info("Search for board: {}", r.statusCode());
				return r.bodyToMono(ContestView.class);
			});
		});
	}

	@Override
	public Mono<PlayerContestStatus> joinContest(UUID playerId, UUID contestId) {
		return accessToken(playerId).map(accessToken -> {
			RequestHeadersSpec<?> spec = getWebClient().post()
					.uri(uriBuilder -> uriBuilder.path(PREFIX + "/board/player")
							.queryParam("player_id", playerId)
							.queryParam("contest_id", contestId)
							.build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken());

			return spec;
		}).flatMap(spec -> {
			return spec.exchangeToMono(r -> {
				if (!r.statusCode().is2xxSuccessful()) {
					throw new IllegalArgumentException("Request rejected: " + r.statusCode());
				}
				log.info("Search for board: {}", r.statusCode());
				return r.bodyToMono(PlayerContestStatus.class);
			});
		});
	}

	@Override
	public Mono<PlayerRawMovesHolder> getExampleMoves(UUID playerId, UUID contestId) {
		return accessToken(playerId).map(accessToken -> {
			RequestHeadersSpec<?> spec = getWebClient().get()
					.uri(uriBuilder -> uriBuilder.path(PREFIX + "/board/moves")
							.queryParam("player_id", playerId)
							.queryParam("contest_id", contestId)
							.build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken());

			return spec;
		}).flatMap(spec -> {
			return spec.exchangeToMono(r -> {
				if (!r.statusCode().is2xxSuccessful()) {
					throw new IllegalArgumentException("Request rejected: " + r.statusCode());
				}
				log.info("Search for moves: {}", r.statusCode());
				return r.bodyToMono(PlayerRawMovesHolder.class);
			});
		});
	}

	@Override
	public Mono<ContestView> playMove(UUID playerId, UUID contestId, Map<String, ?> move) {
		return accessToken(playerId).map(accessToken -> {
			RequestBodySpec spec = getWebClient().post()
					.uri(uriBuilder -> uriBuilder.path(PREFIX + "/board/move")
							.queryParam("player_id", playerId)
							.queryParam("contest_id", contestId)
							.build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken());

			return spec;
		}).flatMap(spec -> {
			return spec.bodyValue(move).exchangeToMono(r -> {
				if (!r.statusCode().is2xxSuccessful()) {
					throw new IllegalArgumentException("Request rejected: " + r.statusCode());
				}
				log.info("Play a move: {}", r.statusCode());
				return r.bodyToMono(ContestView.class);
			});
		});
	}

	@Override
	public Mono<LeaderboardRaw> loadLeaderboard(UUID contestId) {
		return accessToken(defaultPlayerId).map(accessToken -> {
			RequestHeadersSpec<?> spec = getWebClient().get()
					.uri(uriBuilder -> uriBuilder.path(PREFIX + "/leaderboards")
							// .queryParam("player_id", playerId)
							.queryParam("contest_id", contestId)
							.build())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken());

			return spec;
		}).flatMap(spec -> {
			return spec.exchangeToMono(r -> {
				if (!r.statusCode().is2xxSuccessful()) {
					throw new IllegalArgumentException("Request rejected: " + r.statusCode());
				}
				log.info("Play a move: {}", r.statusCode());
				return r.bodyToMono(LeaderboardRaw.class);
			});
		});
	}

}
