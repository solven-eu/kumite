package eu.solven.kumite.lifecycle;

import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.player.ContestPlayersRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContestLifecycleManager {
	@NonNull
	final GamesRegistry gamesStore;
	@NonNull
	final ContestsRegistry contestsStore;

	@NonNull
	final ContestPlayersRegistry contestPlayersRegistry;

}
