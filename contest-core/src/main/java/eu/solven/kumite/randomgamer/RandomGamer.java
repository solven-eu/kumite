package eu.solven.kumite.randomgamer;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.INoOpKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
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

	/**
	 * Join any contest with given policy.
	 */
	public void joinOncePerContestAndPlayer(IContestJoiningStrategy contestJoiningStrategy) {
		joinOncePerContestAndPlayer(GameSearchParameters.builder().build(), contestJoiningStrategy);
	}

	/**
	 * 
	 * @param gameSearch
	 * @param contestJoiningStrategy
	 * @param maxJoin
	 *            this is important not to join 1024 players on optimization games
	 * @return
	 */
	public Map<UUID, Set<UUID>> joinOncePerContestAndPlayer(GameSearchParameters gameSearch,
			IContestJoiningStrategy contestJoiningStrategy) {
		Map<UUID, Set<UUID>> joinedContestToPlayerIds = new ConcurrentHashMap<>();

		gamesRegistry.searchGames(gameSearch).stream().forEach(game -> {
			Map<UUID, Set<UUID>> joined = joinOncePerContestAndPlayer(
					ContestSearchParameters.builder().gameOver(false).acceptPlayers(true).build(),
					contestJoiningStrategy);

			joined.forEach((contestId, playerIds) -> {
				joinedContestToPlayerIds.computeIfAbsent(contestId, k -> new ConcurrentSkipListSet<>())
						.addAll(playerIds);
			});
		});

		return joinedContestToPlayerIds;
	}

	public Map<UUID, Set<UUID>> joinOncePerContestAndPlayer(ContestSearchParameters contestSearch,
			IContestJoiningStrategy contestJoiningStrategy) {
		Map<UUID, Set<UUID>> joinedContestToPlayerIds = new ConcurrentHashMap<>();

		contestsRegistry.searchContests(contestSearch).forEach(contest -> {
			for (UUID playerId : RandomPlayer.playerIds()) {
				PlayerContestStatus playerStatus = contestPlayersRegistry.getPlayingPlayer(playerId, contest);

				boolean canJoin = playerStatus.isPlayerCanJoin();

				if (!canJoin) {
					// given player may be already joined
					continue;
				}

				boolean additionalRandomShouldJoin = contestJoiningStrategy.shouldJoin(contest.getGame(), contest);
				if (!additionalRandomShouldJoin) {
					// Current strategy would not accept more players for given contest
					return;
				}

				log.debug("playerId={} is joining contestId={}", playerId, contest.getContestId());
				PlayerJoinRaw playerRegistrationRaw =
						PlayerJoinRaw.builder().contestId(contest.getContestId()).playerId(playerId).build();
				boardLifecycleManager.registerPlayer(contest, playerRegistrationRaw);

				joinedContestToPlayerIds.computeIfAbsent(contest.getContestId(), k -> new ConcurrentSkipListSet<>())
						.add(playerId);
			}
		});

		return joinedContestToPlayerIds;
	}

	/**
	 * Play joined contest with given policy.
	 */
	public int playOncePerContestAndPlayer() {
		ContestSearchParameters contestSearch = ContestSearchParameters.builder().gameOver(false).build();

		return playOncePerContestAndPlayer(contestSearch, p -> true);
	}

	public int playOncePerContestAndPlayer(ContestSearchParameters contestSearch,
			Predicate<KumitePlayer> acceptPlayer) {
		AtomicInteger nbMoves = new AtomicInteger();

		contestsRegistry.searchContests(contestSearch).forEach(contest -> {
			contest.getPlayers()
					.stream()
					.filter(p -> RandomPlayer.isRandomPlayer(p.getPlayerId()))
					.filter(p -> acceptPlayer.test(p))
					.forEach(player -> {
						IKumiteBoardView boardView = boardsRegistry.makeDynamicBoardHolder(contest.getContestId())
								.get()
								.asView(player.getPlayerId());
						Map<String, IKumiteMove> moves =
								contest.getGame().exampleMoves(randomGenerator, boardView, player.getPlayerId());

						List<Map.Entry<String, IKumiteMove>> playableMoves = moves.entrySet()
								.stream()
								.filter(e -> isPlayableMove(e.getValue()))
								.collect(Collectors.toList());

						if (playableMoves.isEmpty()) {
							log.debug("Not a single playable move");
							return;
						}

						Map.Entry<String, IKumiteMove> chosenMove = pickMove(playableMoves);
						log.debug("playerId={} for contestId={} is playing {}",
								player.getPlayerId(),
								contest.getContestId(),
								chosenMove.getKey());

						nbMoves.incrementAndGet();
						boardLifecycleManager.onPlayerMove(contest,
								PlayerMoveRaw.builder()
										.playerId(player.getPlayerId())
										.move(chosenMove.getValue())
										.build());
					});
		});

		return nbMoves.get();
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
