package eu.solven.kumite.contest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerMoveRaw;
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
	IHasPlayers hasPlayers;

	@NonNull
	IGame game;

	@NonNull
	IHasBoard board;

	@NonNull
	IHasGameover hasGameover;

	@Override
	public List<KumitePlayer> getPlayers() {
		return hasPlayers.getPlayers();
	}

	@Override
	public boolean isGameOver() {
		return hasGameover.isGameOver();
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
						.players(contest.getPlayers().stream().map(p -> p.getPlayerId()).collect(Collectors.toSet()))
						.build())
				.build();
	}

	public void checkValidMove(PlayerMoveRaw playerMove) {
		UUID playerId = playerMove.getPlayerId();
		IKumiteMove move = playerMove.getMove();

		if (getPlayers().stream().noneMatch(p -> p.getPlayerId().equals(playerId))) {
			throw new IllegalArgumentException("playerId=" + playerId + " is not registered");
		} else if (!game.isValidMove(move)) {
			throw new IllegalArgumentException("move=" + move + " is invalid");
		}

		List<String> invalidMoveReasons = board.get().isValidMove(playerMove);
		if (!invalidMoveReasons.isEmpty()) {
			throw new IllegalArgumentException(
					"move=" + move + " by playerId=" + playerId + " is invalid: " + invalidMoveReasons);
		}
	}
}
