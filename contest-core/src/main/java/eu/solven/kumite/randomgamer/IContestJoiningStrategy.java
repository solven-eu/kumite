package eu.solven.kumite.randomgamer;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.game.IGame;

public interface IContestJoiningStrategy {

	boolean shouldJoin(IGame game, Contest contest);

}
