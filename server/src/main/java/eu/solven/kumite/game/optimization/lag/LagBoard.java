package eu.solven.kumite.game.optimization.lag;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.move.PlayerMoveRaw;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class LagBoard implements IKumiteBoard, IKumiteBoardView {

	Map<UUID, Long> playerToLatestLagMs = new ConcurrentHashMap<>();

	@Override
	public List<String> isValidMove(PlayerMoveRaw playerMove) {
		return Collections.emptyList();
	}

	@Override
	public void registerMove(PlayerMoveRaw playerMove) {
		LagServerTimestamp tsmSolution = (LagServerTimestamp) playerMove.getMove();

		long lag = System.currentTimeMillis() - Long.parseLong(tsmSolution.getMoveTimestamp());

		playerToLatestLagMs.put(playerMove.getPlayerId(), lag);
	}

	@Override
	public IKumiteBoardView asView(UUID playerId) {
		return this;
	}

	@Override
	public void registerContender(UUID playerId) {
		// Optimization problems can accept any player
		playerToLatestLagMs.put(playerId, Long.MAX_VALUE);
	}

	@Override
	public List<UUID> snapshotPlayers() {
		return playerToLatestLagMs.keySet().stream().collect(Collectors.toList());
	}
}
