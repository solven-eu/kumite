package eu.solven.kumite.player.gamer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.INoOpKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.PlayerContestStatus;
import eu.solven.kumite.player.PlayerJoinRaw;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This holds a contender logic, playing randomly amongst available moves. It is useful for integration-tests.
 * 
 * @author Benoit Lacelle
 *
 */
@AllArgsConstructor
@Slf4j
public class RandomGamer {
	// Limit the number of contenders as some game can accept any number of players (e.g. optimization games)
	// private int nbPlayers = 16;

	final GamesRegistry gamesRegistry;
	final ContestsRegistry contestsRegistry;
	final ContestPlayersRegistry contestPlayersRegistry;

	final BoardsRegistry boardsRegistry;
	final RandomGenerator randomGenerator;

	final BoardLifecycleManager boardLifecycleManager;

	final IContestJoiningStrategy contestJoiningStrategy = new RandomPlayersVs1();

	/**
	 * Join any contest with given policy.
	 */
	public void doJoin() {
		gamesRegistry.getGames().forEach(game -> {
			contestsRegistry
					.searchContests(ContestSearchParameters.builder().gameOver(false).acceptPlayers(true).build())
					.forEach(contest -> {
						for (UUID playerId : RandomPlayer.playerIds()) {
							PlayerContestStatus playerStatus =
									contestPlayersRegistry.getPlayingPlayer(playerId, contest);

							boolean canJoin = playerStatus.isPlayerCanJoin();

							if (!canJoin) {
								return;
							}

							boolean wantToJoin = contestJoiningStrategy.shouldJoin(game, contest);
							if (!wantToJoin) {
								return;
							}

							log.info("playerId={} is joining contestId={}", playerId, contest.getContestId());
							PlayerJoinRaw playerRegistrationRaw = PlayerJoinRaw.builder()
									.contestId(contest.getContestId())
									.playerId(playerId)
									.build();
							contestPlayersRegistry.registerPlayer(contest, playerRegistrationRaw);
						}
					});
		});
	}

	/**
	 * Play joined contest with given policy.
	 */
	public void doPlay() {
		gamesRegistry.getGames().forEach(game -> {
			contestsRegistry
					.searchContests(ContestSearchParameters.builder().gameOver(false).acceptPlayers(true).build())
					.forEach(contest -> {
						contest.getPlayers()
								.stream()
								.filter(p -> RandomPlayer.isRandomPlayer(p.getPlayerId()))
								.forEach(player -> {
									IKumiteBoardView boardView =
											boardsRegistry.makeDynamicBoardHolder(contest.getContestId())
													.get()
													.asView(player.getPlayerId());
									Map<String, IKumiteMove> moves =
											game.exampleMoves(randomGenerator, boardView, player.getPlayerId());

									List<Map.Entry<String, IKumiteMove>> playableMoves = moves.entrySet()
											.stream()
											.filter(e -> isPlayableMove(e.getValue()))
											.collect(Collectors.toList());

									if (playableMoves.isEmpty()) {
										log.debug("Not a single playable move");
										return;
									}

									Map.Entry<String, IKumiteMove> chosenMove = pickMove(playableMoves);
									log.info("playerId={} for contestId={} is playing {}",
											player.getPlayerId(),
											contest.getContestId(),
											chosenMove.getKey());

									boardLifecycleManager.onPlayerMove(contest,
											PlayerMoveRaw.builder()
													.playerId(player.getPlayerId())
													.move(chosenMove.getValue())
													.build());
								});
					});
		});
	}

	private Entry<String, IKumiteMove> pickMove(List<Entry<String, IKumiteMove>> playableMoves) {
		return playableMoves.get(randomGenerator.nextInt(playableMoves.size()));
	}

	private boolean isPlayableMove(IKumiteMove value) {
		if (value instanceof INoOpKumiteMove) {
			return false;
		} else {
			return true;
		}
	}
}
