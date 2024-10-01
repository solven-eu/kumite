package eu.solven.kumite.account;

import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {

		KumiteServerComponentsConfiguration.class,

		AccountSearchHandler.class,

})
@ActiveProfiles({ IKumiteSpringProfiles.P_INMEMORY })
public class TestKumiteAccountsHandler {
	@Autowired
	AccountSearchHandler accountsSearchHandler;

	@Test
	public void testNoFilter() {
		ServerRequest request = Mockito.mock(ServerRequest.class);

		Assertions.assertThatThrownBy(() -> accountsSearchHandler.searchAccounts(request).block())
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testUnknownAccount() {
		ServerRequest request = Mockito.mock(ServerRequest.class);
		Mockito.when(request.queryParam("account_id")).thenReturn(Optional.of(UUID.randomUUID().toString()));

		// TODO Check the output is empty
		ServerResponse serverResponse = accountsSearchHandler.searchAccounts(request).block();
	}

	@Test
	public void testRandomPlayer() {
		ServerRequest request = Mockito.mock(ServerRequest.class);
		Mockito.when(request.queryParam("account_id")).thenReturn(Optional.of(RandomPlayer.ACCOUNT_ID.toString()));

		// TODO Check the output is empty
		accountsSearchHandler.searchAccounts(request).block();
	}
}
