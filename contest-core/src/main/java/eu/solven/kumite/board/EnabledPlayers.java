package eu.solven.kumite.board;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.INoOpKumiteMove;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EnabledPlayers {
	final ContestsRegistry contestsRegistry;
	final RandomGenerator randomGenerator;

	public Set<UUID> playersCanMove(UUID contestId, IKumiteBoard board) {
		Contest contest = contestsRegistry.getContest(contestId);

		Set<UUID> movablePlayerIds = board.snapshotContenders()
				.stream()
				.filter(playerId -> canPlay(
						contest.getGame().exampleMoves(randomGenerator, board.asView(playerId), playerId)))
				.collect(Collectors.toSet());

		return movablePlayerIds;
	}

	private boolean canPlay(Map<String, IKumiteMove> exampleMoves) {
		return exampleMoves.values().stream().anyMatch(move -> !(move instanceof INoOpKumiteMove));
	}
}
