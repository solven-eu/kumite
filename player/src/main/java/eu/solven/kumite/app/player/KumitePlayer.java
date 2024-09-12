package eu.solven.kumite.app.player;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

@AllArgsConstructor
@Slf4j
public class KumitePlayer implements IKumitePlayer {
	final IKumiteServer kumiteServer;

	/**
	 * Optimization games are the simplest one in term of integration: one just have to publish one solution to get on
	 * the leaderboard
	 * 
	 * @param kumiteServer
	 * @param playerId
	 */
	@Override
	public void playOptimizationGames(UUID playerId) {
		GameSearchParameters optimizationsGameSearch =
				GameSearchParameters.builder().requiredTag(IGameMetadataConstants.TAG_OPTIMIZATION).build();

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
				// Load the board for given contest
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
						return kumiteServer.joinContest(playerId, contestId)
								// We load the board again once we are signed-up
								.flatMap(playingPlayer -> kumiteServer.loadBoard(contestId, playerId));
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
							log.info("No move. We quit the game. contestId=", contestId);
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
				.subscribe();
	}

	/**
	 * 1v1 games needs sone sort of `do-while` loop, to play moves until the game is over.
	 * 
	 * @param kumiteServer
	 * @param playerId
	 */
	@Override
	public void play1v1(UUID playerId) {
		GameSearchParameters optimizationsGameSearch =
				GameSearchParameters.builder().requiredTag(IGameMetadataConstants.TAG_1V1).build();

		Mono.just(optimizationsGameSearch)
				// Search the games
				.flatMapMany(gameSearch -> {
					log.info("Looking for games matching `{}`", gameSearch);
					return kumiteServer.searchGames(gameSearch);
				})
				// Search for contests for given game
				.flatMap(game -> kumiteServer.searchContests(
						ContestSearchParameters.builder().gameId(Optional.of(game.getGameId())).build()))
				// Load the board for given contest
				.flatMap(contest -> kumiteServer.loadBoard(playerId, contest.getContestId()))
				// Filter interesting boards
				.filter(c -> !c.getDynamicMetadata().isGameOver())
				.filter(c -> c.getDynamicMetadata().isAcceptingPlayers())
				// Process each contest
				.flatMap(contestView -> {
					UUID contestId = contestView.getContestId();

					if (contestView.getPlayerStatus().isPlayerHasJoined()) {
						log.info("Received board for already joined contestId={}", contestId);
						return Mono.empty();
					} else if (contestView.getPlayerStatus().isPlayerCanJoin()) {
						log.info("Received board for joinable contestId={}", contestId);
						return kumiteServer.joinContest(playerId, contestId)
								.flatMap(playingPlayer -> kumiteServer.loadBoard(contestId, playerId));
					} else {
						log.info("We can not join contest={}", contestId);
						return Mono.empty();
					}
				})
				// This acts like a `do-while` loop: we loop by playing moves until the game is over
				// https://codersee.com/project-reactor-expand/
				.expand(joinedContestView -> {
					UUID contestId = joinedContestView.getContestId();

					if (joinedContestView.getDynamicMetadata().isGameOver()) {
						log.info("contestId={} is gameOver", contestId);
						return Mono.empty();
					}

					Mono<PlayerRawMovesHolder> exampleMoves =
							kumiteServer.getExampleMoves(joinedContestView.getPlayerStatus().getPlayerId(), contestId);
					Mono<ContestView> monoContestViewPostMove = exampleMoves.flatMap(moves -> {
						Optional<Map<String, ?>> optSelectedMove = selectMove(joinedContestView.getBoard(), moves);

						if (optSelectedMove.isEmpty()) {
							// There is no available move: wait until gameOver
							Duration delay = Duration.ofSeconds(5);
							log.info("No move. We wait {} for {}", delay, contestId);
							return Mono.just(joinedContestView).delayElement(delay);
						}

						Map<String, ?> selectedMove = optSelectedMove.get();
						log.info("We playMove `{}`for {}", selectedMove);
						return kumiteServer.playMove(playerId, joinedContestView.getContestId(), selectedMove);
					});
					return monoContestViewPostMove;
				})
				.flatMap(contestView -> {
					return kumiteServer.loadLeaderboard(contestView.getContestId()).doOnNext(leaderboard -> {
						log.info("contestid={} leaderbord={}", contestView.getContestId(), leaderboard);
					});
				})
				.subscribe();
	}

	/**
	 * Select one move amongst the examples moves.
	 * 
	 * @param board
	 * @param moves
	 * @return the first suggested move.
	 */
	private Optional<Map<String, ?>> selectMove(@NonNull Map<String, ?> board, PlayerRawMovesHolder moves) {
		// WaitForPlayersMove and WaitForSignups would have a `wait:true` flag
		return moves.getMoves().values().stream().filter(m -> !Boolean.TRUE.equals(m.get("wait"))).findAny();
	}
}
