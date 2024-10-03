package eu.solven.kumite.board;

import org.greenrobot.eventbus.EventBus;
import org.springframework.beans.factory.InitializingBean;

import eu.solven.kumite.contest.ContestsRegistry;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BoardGameoverRegistry implements InitializingBean {
	final ContestsRegistry contestsRegistry;
	final EventBus eventBus;

	@Override
	public void afterPropertiesSet() {
		eventBus.register(this);
	}

	// BoardIsUpdated->ContestIsGameover is managed by BoardLifecycleManager
	// @Subscribe
	// public void onBoardUpdate(BoardIsUpdated boardIsUpdated) {
	// UUID contestId = boardIsUpdated.getContestId();
	//
	// Contest contest = contestsRegistry.getContest(contestId);
	// if (contest.getGame().makeDynamicGameover(contest.getBoard()).isGameOver()) {
	// eventBus.post(ContestIsGameover.builder().contestId(contestId).build());
	// }
	// }

}
