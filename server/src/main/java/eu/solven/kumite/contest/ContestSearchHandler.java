package eu.solven.kumite.contest;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.contest.ContestSearchParameters.ContestSearchParametersBuilder;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
public class ContestSearchHandler {
	ContestsStore contestsStore;

	public Mono<ServerResponse> listContests(ServerRequest request) {
		ContestSearchParametersBuilder parameters = ContestSearchParameters.builder();

		Optional<String> optUuid = request.queryParam("contest_uuid");
		optUuid.ifPresent(rawUuid -> parameters.contestUuid(Optional.of(UUID.fromString(rawUuid))));

		Optional<String> optGame = request.queryParam("game_uuid");
		optGame.ifPresent(rawGameUuid -> parameters.gameUuid(Optional.of(UUID.fromString(rawGameUuid))));

		Optional<String> optMorePlayers = request.queryParam("accept_players");
		optMorePlayers.ifPresent(rawMorePlayers -> parameters.acceptPlayers(Boolean.parseBoolean(rawMorePlayers)));

		Optional<String> optBeingPlayed = request.queryParam("being_played");
		optBeingPlayed.ifPresent(rawBeingPlayed -> parameters.beingPlayed(Boolean.parseBoolean(rawBeingPlayed)));

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(contestsStore.searchContests(parameters.build())
						.stream()
						.map(ContestMetadataRaw::snapshot)
						.collect(Collectors.toList())));
	}
}