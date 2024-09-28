package eu.solven.kumite.player.gamer;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestDynamicMetadata;
import eu.solven.kumite.game.IGame;

/**
 * This strategy will fill contests with randomPlayers, leaving one room for a not-random player.
 * 
 * It is useful to test as a single player.
 * 
 * @author Benoit Lacelle
 *
 */
public class RandomPlayersVsThemselves implements IContestJoiningStrategy {

	@Override
	public boolean shouldJoin(IGame game, Contest contest) {
		ContestDynamicMetadata dynamic = Contest.snapshot(contest).getDynamicMetadata();

		if (!dynamic.isRequiringPlayers()) {
			// This contest does not require players: do not join
			return false;
		}

		long nbRandomContenders = dynamic.getContenders()
				.stream()
				.filter(playingPlayerId -> RandomPlayer.isRandomPlayer(playingPlayerId))
				.count();

		int nbMinPlayers = contest.getConstantMetadata().getMinPlayers();

		if (nbRandomContenders < nbMinPlayers) {
			// We accept more random players until the game is playable
			return true;
		}

		return false;
	}

}
