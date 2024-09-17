package eu.solven.kumite.player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.controllers.KumiteHandlerHelper;
import eu.solven.kumite.player.PlayersSearchParameters.PlayersSearchParametersBuilder;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class PlayersSearchHandler {
	final ContestPlayersRegistry contestsPlayersRegistry;
	final IAccountPlayersRegistry accountPlayersRegistry;

	public Mono<ServerResponse> listPlayers(ServerRequest request) {
		PlayersSearchParametersBuilder parametersBuilder = PlayersSearchParameters.builder();

		Optional<String> optContestId = request.queryParam("contest_id");
		optContestId.ifPresent(rawContestId -> parametersBuilder.contestId(Optional.of(UUID.fromString(rawContestId))));

		Optional<String> optAccountId = request.queryParam("account_id");
		optAccountId.ifPresent(rawAccountId -> parametersBuilder.accountId(Optional.of(UUID.fromString(rawAccountId))));

		PlayersSearchParameters search = parametersBuilder.build();

		List<KumitePlayer> players;
		if (optContestId.isPresent()) {
			players = contestsPlayersRegistry.makeDynamicHasPlayers(search.getContestId().get()).getPlayers();
		} else if (optAccountId.isPresent()) {
			players = accountPlayersRegistry.makeDynamicHasPlayers(search.getAccountId().get()).getPlayers();
		} else {
			throw new IllegalArgumentException("Need at least one filtering clause");
		}

		return KumiteHandlerHelper.okAsJson(players);
	}
}