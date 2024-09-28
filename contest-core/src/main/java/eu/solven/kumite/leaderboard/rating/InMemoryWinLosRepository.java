package eu.solven.kumite.leaderboard.rating;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InMemoryWinLosRepository implements IWinLosRatingRepository {
	final Map<PlayerAtGame, Rating1v1> ratings1v1 = new ConcurrentHashMap<>();
	final Map<PlayerAtGame, RatingMultiplayer> ratingsMp = new ConcurrentHashMap<>();

	private PlayerAtGame key(UUID gameId, UUID playerId) {
		return PlayerAtGame.builder().gameId(gameId).playerId(playerId).build();
	}

	@Override
	public void markWin(UUID gameId, UUID playerId) {
		PlayerAtGame key = key(gameId, playerId);
		ratings1v1.computeIfAbsent(key, k -> Rating1v1.builder().build());
		ratings1v1.compute(key, (k, previous) -> previous.markWin());
	}

	@Override
	public void markLos(UUID gameId, UUID playerId) {
		PlayerAtGame key = key(gameId, playerId);
		ratings1v1.computeIfAbsent(key, k -> Rating1v1.builder().build());
		ratings1v1.compute(key, (k, previous) -> previous.markLos());
	}

	@Override
	public void markTie(UUID gameId, UUID playerId) {
		PlayerAtGame key = key(gameId, playerId);
		ratings1v1.computeIfAbsent(key, k -> Rating1v1.builder().build());
		ratings1v1.compute(key, (k, previous) -> previous.markTie());
	}

	@Override
	public void markRank(UUID gameId, UUID playerId, int rank, int nbPlayers) {
		PlayerAtGame key = key(gameId, playerId);
		ratingsMp.computeIfAbsent(key, k -> RatingMultiplayer.builder().build());
		ratingsMp.compute(key, (k, previous) -> previous.markRank(rank, nbPlayers));
	}

}
