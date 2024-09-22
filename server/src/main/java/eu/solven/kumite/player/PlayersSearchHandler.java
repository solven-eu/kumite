package eu.solven.kumite.player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.webflux.api.KumiteHandlerHelper;
import eu.solven.kumite.player.PlayerSearchParameters.PlayerSearchParametersBuilder;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class PlayersSearchHandler {
	final ContestPlayersRegistry contestsPlayersRegistry;
	final IAccountPlayersRegistry accountPlayersRegistry;

	public Mono<ServerResponse> listPlayers(ServerRequest request) {
		PlayerSearchParametersBuilder parametersBuilder = PlayerSearchParameters.builder();

		Optional<UUID> optContestId = KumiteHandlerHelper.optUuid(request, "contest_id");
		optContestId.ifPresent(contestId -> parametersBuilder.contestId(Optional.of(contestId)));

		Optional<UUID> optAccountId = KumiteHandlerHelper.optUuid(request, "account_id");
		optAccountId.ifPresent(accountId -> parametersBuilder.accountId(Optional.of(accountId)));

		Optional<UUID> optPlayerId = KumiteHandlerHelper.optUuid(request, "player_id");
		optPlayerId.ifPresent(playerId -> parametersBuilder.playerId(Optional.of(playerId)));

		PlayerSearchParameters search = parametersBuilder.build();

		List<KumitePlayer> players;
		if (optContestId.isPresent()) {
			players = contestsPlayersRegistry.makeDynamicHasPlayers(search.getContestId().get()).getPlayers();
		} else if (optAccountId.isPresent()) {
			players = accountPlayersRegistry.makeDynamicHasPlayers(search.getAccountId().get()).getPlayers();
		} else if (optPlayerId.isPresent()) {
			UUID playerId = search.getPlayerId().get();
			UUID accountId = accountPlayersRegistry.getAccountId(playerId);
			players = accountPlayersRegistry.makeDynamicHasPlayers(accountId)
					.getPlayers()
					.stream()
					.filter(p -> p.getPlayerId().equals(playerId))
					.collect(Collectors.toList());
		} else {
			throw new IllegalArgumentException("Need at least one filtering clause");
		}

		return KumiteHandlerHelper.okAsJson(players);
	}
}