package eu.solven.kumite.scenario;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.account.AccountsStore;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.contest.ContestLifecycleManager;
import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestsStore;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesStore;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { KumiteServerComponentsConfiguration.class })
public class TestTSMLifecycle {
	@Autowired
	ContestLifecycleManager lifecycleManager;

	@Autowired
	AccountsStore accountsStore;
	
	@Autowired
	GamesStore gamesStore;

	@Autowired
	ContestsStore contestsStore;

	@Test
	public void testSinglePlayer() {
		List<GameMetadata> games =
				gamesStore.searchGames(GameSearchParameters.builder().titlePattern(Optional.of("Salesman")).build());

		Assertions.assertThat(games).hasSize(1);

		List<ContestMetadata> contests = contestsStore.searchContests(
				ContestSearchParameters.builder().gameUuid(Optional.of(games.get(0).getGameId())).build());

		Assertions.assertThat(contests)
				.hasSize(1)
				.element(0)
				.matches(c -> c.isAcceptPlayers())
				.matches(c -> !c.isGameOver());
	}
}
