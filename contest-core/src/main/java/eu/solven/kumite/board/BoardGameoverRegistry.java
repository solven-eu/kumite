package eu.solven.kumite.board;

import java.util.UUID;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.events.BoardIsUpdated;
import eu.solven.kumite.events.ContestIsGameover;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BoardGameoverRegistry {
	final ContestsRegistry contestsRegistry;
	final EventBus eventBus;

	@Subscribe
	public void onBoardUpdate(BoardIsUpdated boardIsUpdated) {
		UUID contestId = boardIsUpdated.getContestId();

		Contest contest = contestsRegistry.getContest(contestId);
		if (contest.getGame().makeDynamicGameover(contest.getBoard()).isGameOver()) {
			eventBus.post(ContestIsGameover.builder().contestId(contestId).build());
		}
	}
}
