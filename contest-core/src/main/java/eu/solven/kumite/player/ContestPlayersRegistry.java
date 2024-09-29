package eu.solven.kumite.player;

import java.util.UUID;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ContestPlayersRegistry {
	final GamesRegistry gamesRegistry;
	final IAccountPlayersRegistry accountPlayersRegistry;

	final IContendersRepository contestPlayersRepository;

	final IViewingAccountsRepository viewingAccountsRepository;

	private void registerViewingPlayer(Contest contest, UUID playerId) {
		if (KumitePlayer.AUDIENCE_PLAYER_ID.equals(playerId) || KumitePlayer.PREVIEW_PLAYER_ID.equals(playerId)) {
			// There is no need to register the public player
			return;
		}

		UUID accountId = accountPlayersRegistry.getAccountId(playerId);
		viewingAccountsRepository.registerViewer(contest.getContestId(), accountId);
	}

	public void registerPlayer(Contest contest, PlayerJoinRaw playerRegistrationRaw) {
		UUID playerId = playerRegistrationRaw.getPlayerId();

		if (playerRegistrationRaw.isViewer()) {
			registerViewingPlayer(contest, playerId);
		} else {
			registerContender(contest, playerId);
		}
	}

	private void registerContender(Contest contest, UUID playerId) {
		if (KumitePlayer.AUDIENCE_PLAYER_ID.equals(playerId) || KumitePlayer.PREVIEW_PLAYER_ID.equals(playerId)) {
			// This should have been handled before, while verifying authenticated account can play given playerId
			throw new IllegalArgumentException("Public player is not allowed to play");
		}

		UUID contestId = contest.getContestId();

		if (!contest.isAcceptingPlayers()) {
			// If `isAcceptPlayer` is false, currentAccount should not even consider this game.
			throw new IllegalStateException("contestId=" + contestId + " does not accept player");
		} else if (contest.hasPlayerId(playerId)) {
			// This search-API may consider contest with a player from current account as ineligible
			throw new IllegalStateException("contestId=" + contestId + " already includes playerId=" + playerId);
		}

		IGame game = gamesRegistry.getGame(contest.getGameMetadata().getGameId());

		KumitePlayer player = makePlayer(playerId);

		IKumiteBoard updatedBoard;

		// No need to synchronize as BoardLifecycleManager ensure single-threaded per contest
		if (game.canAcceptPlayer(contest, player)) {
			boolean registeredInBoard = contestPlayersRepository.registerContender(contestId, playerId);
			if (registeredInBoard) {
				log.debug(
						"Skip `board.registerContender` as already managed by `contestPlayersRepository.registerContender`");
			} else {
				updatedBoard = contest.getBoard().get();
				try {
					updatedBoard.registerContender(playerId);
				} catch (Throwable t) {
					// What should we do about contestPlayersRegistry? Remove the player? Force gameOver? Drop
					// contestPlayersRegistry and rely only on the board?
					throw new IllegalStateException(
							"Issue after registering a player, but before registering it on the board",
							t);
				}

				throw new IllegalStateException("Unclear how we should persist the new board");
			}
		} else {
			throw new IllegalArgumentException(
					"player=" + playerId + " can not be registered on contestId=" + contestId);
		}

		// We may want to prevent a player to register into too many contests
		long nbPlayingGames = contestPlayersRepository.getContestIds(playerId);
		log.info("playerId={} has joined as contender into contestId={}. (Now playing {} contests)",
				playerId,
				contestId,
				nbPlayingGames);

		// return updatedBoard;
	}

	private KumitePlayer makePlayer(UUID playerId) {
		UUID accountId = accountPlayersRegistry.getAccountId(playerId);
		KumitePlayer player = KumitePlayer.builder().playerId(playerId).accountId(accountId).build();
		return player;
	}

	public IHasPlayers makeDynamicHasPlayers(UUID contestId) {
		return contestPlayersRepository.makeDynamicHasPlayers(contestId);
	}

	/**
	 * 
	 * @param contestId
	 * @param playerId
	 * @return true if given player owning account is already viewing this contest.
	 */
	public boolean isViewing(UUID contestId, UUID playerId) {
		UUID accountId = accountPlayersRegistry.getAccountId(playerId);

		return viewingAccountsRepository.isViewing(contestId, accountId);
	}

	public boolean isRegisteredPlayer(UUID contestId, UUID playerId) {
		return contestPlayersRepository.isContender(contestId, playerId);
	}

	public PlayerContestStatus getPlayingPlayer(UUID playerId, Contest contestMetadata) {
		UUID contestId = contestMetadata.getContestId();

		boolean playerHasJoined = isRegisteredPlayer(contestId, playerId);

		boolean accountIsViewing;
		boolean playerCanJoin;

		if (playerHasJoined) {
			// A player can not join twice a contest
			playerCanJoin = false;
			// A player can not both play and view
			accountIsViewing = false;
		} else {
			accountIsViewing = isViewing(contestId, playerId);

			if (accountIsViewing) {
				// A player can not join if it is viewing
				playerCanJoin = false;
			} else {
				IGame game = gamesRegistry.getGame(contestMetadata.getGameMetadata().getGameId());

				// BEWARE We need a mechanism to prevent one player to register to too many games
				KumitePlayer player = makePlayer(playerId);
				playerCanJoin = game.canAcceptPlayer(contestMetadata, player);
			}
		}

		PlayerContestStatus playingPlayer = PlayerContestStatus.builder()
				.playerId(playerId)
				.playerHasJoined(playerHasJoined)
				.playerCanJoin(playerCanJoin)
				.accountIsViewing(accountIsViewing)
				.build();

		return playingPlayer;

	}

	public void forceGameover(UUID contestId) {
		// viewingAccountsRepository.remove(contestId);
		contestPlayersRepository.gameover(contestId);
	}
}
