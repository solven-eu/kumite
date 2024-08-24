package eu.solven.kumite.leaderboard;

import java.util.UUID;

import lombok.Value;

@Value
public class PlayerDoubleScore implements IPlayerScore {
	UUID playerId;
	double score;
}
