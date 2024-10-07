package eu.solven.kumite.player;

import java.util.Optional;
import java.util.UUID;

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

	final IContendersRepository contendersRepository;

	final IViewingAccountsRepository viewingAccountsRepository;

	private void registerViewingPlayer(Contest contest, UUID playerId) {
		if (KumitePlayer.AUDIENCE_PLAYER_ID.equals(playerId) || KumitePlayer.PREVIEW_PLAYER_ID.equals(playerId)) {
			// There is no need to register the public player
			return;
		}

		UUID accountId = accountPlayersRegistry.getAccountId(playerId);
		viewingAccountsRepository.registerViewer(contest.getContestId(), accountId);
	}

	/**
	 * 
	 * @param contest
	 * @param playerRegistrationRaw
	 * @return the boardStateId if this registered a contender. Empty if this is a viewer.
	 */
	public Optional<UUID> registerPlayer(Contest contest, PlayerJoinRaw playerRegistrationRaw) {
		UUID playerId = playerRegistrationRaw.getPlayerId();

		if (playerRegistrationRaw.isViewer()) {
			registerViewingPlayer(contest, playerId);
			return Optional.empty();
		} else {
			return Optional.of(registerContender(contest, playerId));
		}
	}

	private UUID registerContender(Contest contest, UUID playerId) {
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

		UUID boardStateId;

		// No need to synchronize as BoardLifecycleManager ensure single-threaded per contest
		if (game.canAcceptPlayer(contest, player)) {
			boardStateId = contendersRepository.registerContender(contestId, playerId);
		} else {
			throw new IllegalArgumentException(
					"player=" + playerId + " can not be registered on contestId=" + contestId);
		}

		// We may want to prevent a player to register into too many contests
		long nbPlayingGames = contendersRepository.getContestIds(playerId);
		log.info("playerId={} has joined as contender into contestId={}. (Now playing {} contests)",
				playerId,
				contestId,
				nbPlayingGames);

		return boardStateId;
	}

	private KumitePlayer makePlayer(UUID playerId) {
		UUID accountId = accountPlayersRegistry.getAccountId(playerId);
		KumitePlayer player = KumitePlayer.builder().playerId(playerId).accountId(accountId).build();
		return player;
	}

	public IHasPlayers makeDynamicHasPlayers(UUID contestId) {
		return contendersRepository.hasPlayers(contestId);
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
		return contendersRepository.isContender(contestId, playerId);
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

	public void gameover(UUID contestId) {
		viewingAccountsRepository.gameover(contestId);
		contendersRepository.gameover(contestId);
	}
}
