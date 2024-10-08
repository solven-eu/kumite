package eu.solven.kumite.board;

import java.util.UUID;

public interface ICanGameover {
	UUID doGameover(UUID contestId, boolean force);
}
