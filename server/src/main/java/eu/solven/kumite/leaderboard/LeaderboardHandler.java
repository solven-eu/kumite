package eu.solven.kumite.leaderboard;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.controllers.KumiteHandlerHelper;
import eu.solven.kumite.leaderboard.LeaderboardSearchParameters.LeaderboardSearchParametersBuilder;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
public class LeaderboardHandler {
	LeaderboardRegistry leaderboardStore;

	public Mono<ServerResponse> listScores(ServerRequest request) {
		LeaderboardSearchParametersBuilder parameters = LeaderboardSearchParameters.builder();

		parameters.contestId(KumiteHandlerHelper.uuid(request, "contest_id"));

		// Optional<String> optRank = request.queryParam("rank");
		// optRank.ifPresent(rawRank -> parameters.rank(OptionalInt.of(Integer.parseInt(rawRank))));
		//
		// Optional<String> optMorePlayers = request.queryParam("player_id");
		// optMorePlayers.ifPresent(rawPlayerId -> parameters.playerId(Optional.of(UUID.fromString(rawPlayerId))));

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(leaderboardStore.searchLeaderboard(parameters.build())));
	}
}