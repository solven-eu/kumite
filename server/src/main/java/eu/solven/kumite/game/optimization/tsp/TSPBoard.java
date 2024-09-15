package eu.solven.kumite.game.optimization.tsp;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerMoveRaw;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TSPBoard implements IKumiteBoard {

	@NonNull
	TSPProblem problem;

	Map<UUID, TSPSolution> playerToLatestSolution = new ConcurrentHashMap<>();

	@Override
	public List<String> isValidMove(PlayerMoveRaw playerMove) {
		return problem.isValidMove(playerMove);
	}

	@Override
	public void registerMove(PlayerMoveRaw playerMove) {
		TSPSolution tsmSolution = (TSPSolution) playerMove.getMove();
		playerToLatestSolution.put(playerMove.getPlayerId(), tsmSolution);
	}

	@Override
	public IKumiteBoardView asView(UUID playerId) {
		// We must not return all players solutions to each player
		return problem;
	}

	@Override
	public void registerPlayer(UUID playerId) {
		playerToLatestSolution.put(playerId, TSPSolution.EMPTY);
	}

	@Override
	public List<KumitePlayer> snapshotPlayers() {
		return playerToLatestSolution.keySet().stream().map(KumitePlayer::fromPlayerId).collect(Collectors.toList());
	}
}
