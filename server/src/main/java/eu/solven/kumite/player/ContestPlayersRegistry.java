package eu.solven.kumite.player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import lombok.Value;

@Value
public class ContestPlayersRegistry {
	GamesRegistry gamesStore;

	Map<UUID, Set<UUID>> contestToPlayers = new ConcurrentHashMap<>();

	public void registerPlayer(ContestMetadata contest, KumitePlayer player) {
		IGame game = gamesStore.getGame(contest.getGameMetadata().getGameId());

		UUID playerId = player.getPlayerId();
		if (!contest.isAcceptPlayers()) {
			// If `isAcceptPlayer` is false, currentAccount should not even consider this game.
			throw new IllegalStateException("contestId=" + contest.getContestId() + " does not accept player");
		} else if (contest.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId))) {
			// This search-API may consider contest with a player from current account as ineligible
			throw new IllegalStateException(
					"contestId=" + contest.getContestId() + " already includes player=" + player);
		}

		// We have to synchronous `game.canAcceptPlayer` and playerRegistration
		synchronized (this) {
			if (game.canAcceptPlayer(contest, player)) {
				contestToPlayers.computeIfAbsent(contest.getContestId(), k -> new ConcurrentSkipListSet<>())
						.add(playerId);
			} else {
				throw new IllegalArgumentException("player=" + player + " can not be registered");
			}
		}
	}

	public IHasPlayers makeDynamicHasPlayers(UUID contestId) {
		return () -> contestToPlayers.getOrDefault(contestId, Set.of())
				.stream()
				.sorted()
				// playerName?
				.map(playerId -> KumitePlayer.builder().playerId(playerId).build())
				.collect(Collectors.toList());
	}

	public boolean isRegisteredPlayer(UUID contestId, UUID playerId) {
		return contestToPlayers.getOrDefault(contestId, Set.of()).contains(playerId);
	}
}
