package eu.solven.kumite.contest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IHasBoardMetadata;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.KumitePlayer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Contest implements IContest {
	@NonNull
	UUID contestId;

	@NonNull
	ContestCreationMetadata constantMetadata;

	@Getter(value = AccessLevel.NONE)
	IHasPlayers players;

	@NonNull
	IGame game;

	@NonNull
	IHasBoard board;

	@NonNull
	IHasBoardMetadata boardMetadata;

	@NonNull
	IHasGameover gameover;

	// These are the contenders of the contest
	@Override
	public List<KumitePlayer> getPlayers() {
		return players.getPlayers();
	}

	@Override
	public boolean isGameOver() {
		return gameover.isGameOver();
	}

	@Override
	public GameMetadata getGameMetadata() {
		return game.getGameMetadata();
	}

	public static ContestMetadataRaw snapshot(Contest contest) {
		return ContestMetadataRaw.builder()
				.contestId(contest.getContestId())
				.constantMetadata(contest.getConstantMetadata())
				.dynamicMetadata(ContestDynamicMetadata.builder()
						.acceptingPlayers(contest.isAcceptingPlayers())
						.requiringPlayers(contest.isRequiringPlayers())
						.gameOver(contest.isGameOver())
						.contenders(contest.getPlayers().stream().map(p -> p.getPlayerId()).collect(Collectors.toSet()))
						.build())
				.build();
	}

	/**
	 * 
	 * @param contest
	 * @param boardViewPostMove
	 * @return
	 */
	// public static ContestDynamicMetadata snapshot(Contest contest, IKumiteBoardView boardViewPostMove) {
	// return ContestDynamicMetadata.builder()
	// .acceptingPlayers(contest.isAcceptingPlayers())
	// .requiringPlayers(contest.isRequiringPlayers())
	// .gameOver(boardViewPostMove.isGameOver())
	// .contenders(contest.getPlayers().stream().map(p -> p.getPlayerId()).collect(Collectors.toSet()))
	// .build();
	// }

	public void checkValidMove(PlayerMoveRaw playerMove) {
		UUID playerId = playerMove.getPlayerId();
		IKumiteMove move = playerMove.getMove();

		if (!hasPlayerId(playerId)) {
			throw new IllegalArgumentException("playerId=" + playerId + " is not registered");
		}

		List<String> invalidMoveReasons = game.invalidMoveReasons(board.get().asView(playerId), playerMove);
		if (!invalidMoveReasons.isEmpty()) {
			throw new IllegalArgumentException(
					"move=" + move + " by playerId=" + playerId + " is invalid: " + invalidMoveReasons);
		}
	}
}
