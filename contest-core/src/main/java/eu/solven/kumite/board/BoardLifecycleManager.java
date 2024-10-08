package eu.solven.kumite.board;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.events.PlayerJoinedBoard;
import eu.solven.kumite.events.PlayerMoved;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.PlayerJoinRaw;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter(value = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Slf4j
public class BoardLifecycleManager implements IBoardLifecycleManager {
	final BoardLifecycleManagerHelper helper;

	final BoardMutator boardMutator;

	/**
	 * 
	 * @param contest
	 * @param playerRegistrationRaw
	 * @return
	 */
	@Override
	public IKumiteBoardViewWrapper registerPlayer(Contest contest, PlayerJoinRaw playerRegistrationRaw) {
		UUID contestId = contest.getContestId();
		UUID playerId = playerRegistrationRaw.getPlayerId();

		if (helper.isGameover(contestId)) {
			// Leave before entering the executor if possible
			throw new IllegalStateException("The game is over");
		}

		BoardSnapshotPostEvent boardSnapshot = boardMutator.executeBoardChange(contestId, board -> {
			if (helper.isGameover(contestId)) {
				// Check again for gameOver as it may have been registered since previous check was out of the
				// boardEvolutionThread
				throw new IllegalStateException("The game is over");
			}

			// contestPlayersRegistry takes in charge the record in the board in IBoardRepository
			Optional<UUID> optBoardStateId =
					helper.getContestPlayersRegistry().registerPlayer(contest, playerRegistrationRaw);

			// may be empty in case of viewer
			return optBoardStateId
					.orElseGet(() -> helper.getBoardRegistry().getMetadata(contestId).get().getBoardStateId());
		}, this::doGameover);

		IKumiteBoardView boardViewPostMove = boardSnapshot.getBoard().asView(playerId);

		// We submit the event out of threadPool.
		// Hence we are guaranteed the event is fully processed.
		// The event subscriber can process it synchronously (through beware of deep-stack in case of long event-chains)
		// Hence we do not guarantee other events interleaved when the event is processed
		helper.getEventBus().post(PlayerJoinedBoard.builder().contestId(contestId).playerId(playerId).build());

		return KumiteBoardViewWrapper.builder()
				.boardStateId(boardSnapshot.getBoardStateId())
				.view(boardViewPostMove)
				.build();
	}

	protected void precheckOnPlayerMove(UUID contestId, UUID playerId) {
		if (helper.isGameover(contestId)) {
			// Leave before entering the executor if possible
			throw new IllegalStateException("The game is over");
		}

		if (!helper.getContestPlayersRegistry().isRegisteredPlayer(contestId, playerId)) {
			List<UUID> contestPlayers = helper.getContestPlayersRegistry()
					.makeDynamicHasPlayers(contestId)
					.getPlayers()
					.stream()
					.map(p -> p.getPlayerId())
					.collect(Collectors.toList());
			throw new IllegalArgumentException("playerId=" + playerId
					+ " is not registered in contestId="
					+ contestId
					+ " Registered players: "
					+ contestPlayers);
		}
	}

	@Override
	public IKumiteBoardViewWrapper onPlayerMove(Contest contest, PlayerMoveRaw playerMove) {
		UUID contestId = contest.getContestId();
		UUID playerId = playerMove.getPlayerId();

		// Precheck can be done eagerly
		precheckOnPlayerMove(contestId, playerId);

		BoardSnapshotPostEvent boardSnapshot = boardMutator.executeBoardChange(contestId, currentBoard -> {
			// Precheck can be ensured in boardEvolutionThead
			precheckOnPlayerMove(contestId, playerId);

			// First `.checkMove`: these are generic checks (e.g. is the gamerOver?)
			try {
				contest.checkValidMove(playerMove);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Issue on contest=" + contest + " for move=" + playerMove, e);
			}

			log.info("Registering move for contestId={} by playerId={}", contestId, playerId);

			// This may still fail (e.g. the move is illegal given game rules)
			currentBoard.registerMove(playerMove);

			saveUpdatedBoard(contestId, currentBoard);
			return helper.getBoardRegistry().registerPlayerMoved(contestId, playerId);
		}, this::doGameover);

		IKumiteBoard boardAfter = boardSnapshot.getBoard();

		helper.getEventBus().post(PlayerMoved.builder().contestId(contestId).playerId(playerId).build());

		return KumiteBoardViewWrapper.builder()
				.view(boardAfter.asView(playerId))
				.boardStateId(boardSnapshot.getBoardStateId())
				.build();
	}

	/**
	 * This has to be called from the boardEvolutionThread
	 * 
	 * @param contestId
	 * @param board
	 */
	protected void saveUpdatedBoard(UUID contestId, IKumiteBoard board) {
		// Persist the board (e.g. for concurrent changes)
		helper.getBoardRegistry().updateBoard(contestId, board);

		if (helper.isGameover(contestId)) {
			doGameover(contestId, false);
		}
	}

	protected UUID doGameover(UUID contestId, boolean force) {
		// Mark gameOver while inside the loop. It will prevent other interactions
		log.info("doGameOver for contestId={} force={}", contestId, force);
		return helper.doGameOver(contestId, force);
	}

	@Override
	public UUID forceGameOver(Contest contest) {
		UUID contestId = contest.getContestId();

		IHasBoardMetadata hasMetadata = helper.getBoardRegistry().getMetadata(contestId);
		if (helper.isGameover(contestId)) {
			log.info("contestId={} is already gameOver", contestId);
			return hasMetadata.get().getBoardStateId();
		}

		BoardSnapshotPostEvent snapshot = boardMutator.executeBoardChange(contestId, board -> {
			if (helper.isGameover(contestId)) {
				log.info("contestId={} is already gameOver", contestId);
				return hasMetadata.get().getBoardStateId();
			}

			return doGameover(contestId, true);
		}, this::doGameover);

		// Gameover event is managed by `executeBoardChange`

		return snapshot.getBoardStateId();
	}

}
