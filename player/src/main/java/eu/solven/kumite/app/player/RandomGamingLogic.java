package eu.solven.kumite.app.player;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import eu.solven.kumite.app.server.IKumiteServer;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.player.PlayerContestStatus;
import eu.solven.kumite.player.PlayerRawMovesHolder;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * This {@link IGamingLogic} will pick a random move amongst the ones listed in `exampleMoves`.
 * 
 * @author Benoit Lacelle
 *
 */
@AllArgsConstructor
@Slf4j
public class RandomGamingLogic implements IGamingLogic {
	final IKumiteServer kumiteServer;

	@Override
	public Set<UUID> playOptimizationGames(UUID playerId) {
		GameSearchParameters optimizationsGameSearch =
				GameSearchParameters.builder().requiredTag(IGameMetadataConstants.TAG_OPTIMIZATION).build();

		Set<UUID> playedContestIds = new ConcurrentSkipListSet<>();

		// Given a list of human-friendly game title
		// Flux.fromStream(Stream.of("Travelling Salesman Problem", "Tic-Tac-Toe"))
		// Build a SearchGames query
		// .map(gameTitle -> ))
		Mono.just(optimizationsGameSearch)
				// Search the games
				.flatMapMany(gameSearch -> {
					log.info("Looking for games matching `{}`", gameSearch);
					return kumiteServer.searchGames(gameSearch);
				})
				// Search for contests for given game
				.flatMap(game -> kumiteServer.searchContests(
						ContestSearchParameters.builder().gameId(Optional.of(game.getGameId())).build()))
				// Load the previewBoard for given contest
				.flatMap(contest -> kumiteServer.loadBoard(playerId, contest.getContestId()))
				// Filter interesting boards
				.filter(c -> !c.getDynamicMetadata().isGameOver())
				.filter(c -> c.getDynamicMetadata().isAcceptingPlayers())
				// Process each contest
				.flatMap(contestView -> {
					UUID contestId = contestView.getContestId();
					PlayerContestStatus playerStatus = contestView.getPlayerStatus();

					if (playerStatus.isPlayerHasJoined()) {
						log.info("Received board for already joined contestId={}", contestId);
						return Mono.empty();
					} else if (contestView.getPlayerStatus().isPlayerCanJoin()) {
						log.info("Received board for joinable contestId={}", contestId);

						playedContestIds.add(contestId);

						return kumiteServer.joinContest(playerId, contestId)
								// We load the board again once we are signed-up
								.flatMap(playingPlayer -> kumiteServer.loadBoard(playerId, contestId));
					} else {
						log.info("We can not join contest={}", contestId);
						return Mono.empty();
					}
				})
				.flatMap(joinedContestView -> {
					UUID contestId = joinedContestView.getContestId();

					if (joinedContestView.getDynamicMetadata().isGameOver()) {
						log.info("contestId={} is gameOver", contestId);
						return Mono.empty();
					}

					Mono<PlayerRawMovesHolder> exampleMoves =
							kumiteServer.getExampleMoves(joinedContestView.getPlayerStatus().getPlayerId(), contestId);

					return exampleMoves.flatMap(moves -> {
						Optional<Map<String, ?>> selectedMove = selectMove(joinedContestView.getBoard(), moves);

						if (selectedMove.isEmpty()) {
							log.info("No move. We quit the game. contestId={}", contestId);
							return Mono.empty();
						}

						return kumiteServer.playMove(playerId, joinedContestView.getContestId(), selectedMove.get());
					});
				})
				.flatMap(contestView -> {
					return kumiteServer.loadLeaderboard(contestView.getContestId()).doOnNext(leaderboard -> {
						log.info("contestid={} leaderbord={}", contestView.getContestId(), leaderboard);
					});
				})
				// https://stackoverflow.com/questions/48583716/reactor-groupedflux-wait-to-complete
				.blockLast();

		return playedContestIds;
	}

	/**
	 * 1v1 games needs a `do-while` loop, to play moves until the game is over.
	 * 
	 * @param kumiteServer
	 * @param playerId
	 */
	@Override
	public Set<UUID> play1v1TurnBasedGames(UUID playerId) {
		GameSearchParameters optimizationsGameSearch = GameSearchParameters.builder()
				.requiredTag(IGameMetadataConstants.TAG_1V1)
				.requiredTag(IGameMetadataConstants.TAG_TURNBASED)
				.build();

		Set<UUID> playedContestIds = new ConcurrentSkipListSet<>();

		Mono.just(optimizationsGameSearch)
				// Search the games
				.flatMapMany(gameSearch -> {
					log.info("playerId={} looking for games matching `{}`", playerId, gameSearch);
					return kumiteServer.searchGames(gameSearch);
				})
				// Search for contests for given game
				.flatMap(game -> {
					ContestSearchParameters contestSearch =
							ContestSearchParameters.builder().gameId(Optional.of(game.getGameId())).build();
					log.info("playerId={} looking for contests matching `{}` ({})",
							playerId,
							contestSearch,
							game.getTitle());
					return kumiteServer.searchContests(contestSearch);
				})
				// Load the previewBoard for given contest
				.flatMap(contest -> kumiteServer.loadBoard(playerId, contest.getContestId()))
				// Filter interesting boards
				.filter(c -> !c.getDynamicMetadata().isGameOver())
				.filter(c -> c.getDynamicMetadata().isAcceptingPlayers())
				// Process each contest
				.flatMap(contestView -> {
					UUID contestId = contestView.getContestId();

					if (contestView.getPlayerStatus().isPlayerHasJoined()) {
						log.info("playerId={} received board for already joined contestId={}", playerId, contestId);
						return Mono.empty();
					} else if (contestView.getPlayerStatus().isPlayerCanJoin()) {
						log.info("playerId={} received board for joinable contestId={}", playerId, contestId);

						playedContestIds.add(contestId);

						return kumiteServer.joinContest(playerId, contestId)
								.flatMap(playingPlayer -> kumiteServer.loadBoard(playerId, contestId));
					} else {
						log.info("playerId={} can not join contest={}", playerId, contestId);
						return Mono.empty();
					}
				})
				// This acts like a `do-while` loop: we loop by playing moves until the game is over
				// https://codersee.com/project-reactor-expand/
				.expand(joinedContestView -> {
					UUID contestId = joinedContestView.getContestId();

					if (joinedContestView.getDynamicMetadata().isGameOver()) {
						log.info("playerId={} contestId={} is gameOver", playerId, contestId);
						return Mono.empty();
					}

					Mono<PlayerRawMovesHolder> exampleMoves =
							kumiteServer.getExampleMoves(joinedContestView.getPlayerStatus().getPlayerId(), contestId);
					Mono<ContestView> monoContestViewPostMove = exampleMoves.flatMap(moves -> {
						Optional<Map<String, ?>> optSelectedMove = selectMove(joinedContestView.getBoard(), moves);

						if (optSelectedMove.isEmpty()) {
							// There is no available move: wait until gameOver
							Duration delay = waitDurationIfNoMove();
							log.info("playerId={} has no example move. We wait {} for {}", playerId, delay, contestId);
							return kumiteServer.loadBoard(playerId, contestId).delayElement(delay).doOnNext(view -> {
								log.info("playerId={} has gameOver={} after the pause due to no move",
										playerId,
										view.getDynamicMetadata().isGameOver());
							});
						}

						Map<String, ?> selectedMove = optSelectedMove.get();
						log.info("playerId={} plays contestId={}. Move=`{}`", playerId, contestId, selectedMove);
						return kumiteServer.playMove(playerId, joinedContestView.getContestId(), selectedMove);
					});
					return monoContestViewPostMove;
				})
				.flatMap(contestView -> {
					UUID contestId = contestView.getContestId();
					return kumiteServer.loadLeaderboard(contestId).doOnNext(leaderboard -> {
						log.info("playerId={} contestId={} leaderboard={}", playerId, contestId, leaderboard);
					});
				})
				// https://stackoverflow.com/questions/48583716/reactor-groupedflux-wait-to-complete
				.blockLast();

		return playedContestIds;
	}

	/**
	 * Too large: we may wait too much for the other players.
	 * 
	 * Too small: we induce too much long of the server.
	 * 
	 * @return the duration to sleep before polling again for exampleMoves.
	 */
	protected Duration waitDurationIfNoMove() {
		return Duration.ofMillis(10);
	}

	/**
	 * Select one move amongst the examples moves.
	 * 
	 * @param board
	 * @param moves
	 * @return the first suggested move.
	 */
	protected Optional<Map<String, ?>> selectMove(@NonNull Map<String, ?> board, PlayerRawMovesHolder moves) {
		// WaitForPlayersMove and WaitForSignups would have a `wait:true` flag
		return moves.getMoves().values().stream().filter(m -> !Boolean.TRUE.equals(m.get("wait"))).findAny();
	}
}
