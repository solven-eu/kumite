package eu.solven.kumite.player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

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

	// Once a player is viewing, it can not play as it got some private information about the game.
	// The public information of a board is available by querying the board with playerId=KumitePlayer.PUBLIC
	// A cheater could use 2 accounts: one to look at public information, the other to actually play the game
	final Map<UUID, Set<UUID>> contestToViewingAccounts = new ConcurrentHashMap<>();

	private void registerViewingPlayer(Contest contest, UUID playerId) {
		if (KumitePlayer.AUDIENCE_PLAYER_ID.equals(playerId) || KumitePlayer.PREVIEW_PLAYER_ID.equals(playerId)) {
			// There is no need to register the public player
			return;
		}

		UUID accountId = accountPlayersRegistry.getAccountId(playerId);
		contestToViewingAccounts.computeIfAbsent(contest.getContestId(), k -> new ConcurrentSkipListSet<>())
				.add(accountId);
	}

	public void registerPlayer(Contest contest, PlayerJoinRaw playerRegistrationRaw) {
		UUID playerId = playerRegistrationRaw.getPlayerId();

		if (playerRegistrationRaw.isViewer()) {
			registerViewingPlayer(contest, playerId);
		} else {
			registerPlayingPlayer(contest, playerId);
		}
	}

	private void registerPlayingPlayer(Contest contest, UUID playerId) {
		if (KumitePlayer.AUDIENCE_PLAYER_ID.equals(playerId) || KumitePlayer.PREVIEW_PLAYER_ID.equals(playerId)) {
			// This should have been handled before, while verifying authenticated account can play given playerId
			throw new IllegalArgumentException("Public player is not allowed to play");
		}

		UUID contestId = contest.getContestId();

		if (!contest.isAcceptingPlayers()) {
			// If `isAcceptPlayer` is false, currentAccount should not even consider this game.
			throw new IllegalStateException("contestId=" + contestId + " does not accept player");
		} else if (contest.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId))) {
			// This search-API may consider contest with a player from current account as ineligible
			throw new IllegalStateException("contestId=" + contestId + " already includes playerId=" + playerId);
		}

		IGame game = gamesRegistry.getGame(contest.getGameMetadata().getGameId());

		// No need to synchronize as BoardLifecycleManager ensure single-threaded per contest
		{
			if (game.canAcceptPlayer(contest, KumitePlayer.builder().playerId(playerId).build())) {
				contestPlayersRepository.registerContender(contestId, playerId);
			} else {
				throw new IllegalArgumentException(
						"player=" + playerId + " can not be registered on contestId=" + contestId);
			}
		}

		// We may want to prevent a player to register into too many contests
		long nbPlayingGames = -1;
		// contestToPlayingPlayers.values().stream().filter(players -> players.contains(playerId)).count();
		log.info("playerId={} has registered into contestId={}. Now playing {} contests",
				playerId,
				contestId,
				nbPlayingGames);
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

		return contestToViewingAccounts.getOrDefault(contestId, Collections.emptySet()).contains(accountId);
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
				playerCanJoin =
						game.canAcceptPlayer(contestMetadata, KumitePlayer.builder().playerId(playerId).build());
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
}
