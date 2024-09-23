package eu.solven.kumite.player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InMemoryContestPlayersRepository implements IContendersRepository {

	final Map<UUID, Set<UUID>> contestToContenders = new ConcurrentHashMap<>();

	@Override
	public boolean registerContender(UUID contestId, UUID playerId) {
		boolean addPlayer =
				contestToContenders.computeIfAbsent(contestId, k -> new ConcurrentSkipListSet<>()).add(playerId);

		if (addPlayer) {
			throw new IllegalStateException("playerId=" + playerId + " is already registered in " + contestId);
		}

		return false;
	}

	private Set<UUID> viewContenders(UUID contestId) {
		return contestToContenders.getOrDefault(contestId, Set.of());
	}

	@Override
	public IHasPlayers makeDynamicHasPlayers(UUID contestId) {
		return () -> viewContenders(contestId).stream()
				.sorted()
				.map(playerId -> KumitePlayer.builder().playerId(playerId).build())
				.collect(Collectors.toList());
	}

	@Override
	public boolean isContender(UUID contestId, UUID playerId) {
		return viewContenders(contestId).stream().anyMatch(somePlayerId -> somePlayerId.equals(playerId));
	}

}
