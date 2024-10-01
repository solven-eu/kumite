package eu.solven.kumite.player;

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

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {

		KumiteServerComponentsConfiguration.class,

		PlayersSearchHandler.class,

})
@ActiveProfiles({ IKumiteSpringProfiles.P_INMEMORY })
public class TestKumitePlayerHandler {
	@Autowired
	PlayersSearchHandler playersSearchHandler;

	@Test
	public void testNoFilter() {
		ServerRequest request = Mockito.mock(ServerRequest.class);

		Assertions.assertThatThrownBy(() -> playersSearchHandler.listPlayers(request).block())
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testUnknownPlayer() {
		ServerRequest request = Mockito.mock(ServerRequest.class);
		Mockito.when(request.queryParam("player_id")).thenReturn(Optional.of(UUID.randomUUID().toString()));

		// TODO Check the output is empty
		playersSearchHandler.listPlayers(request).block();
	}

	@Test
	public void testRandomPlayer() {
		ServerRequest request = Mockito.mock(ServerRequest.class);
		Mockito.when(request.queryParam("player_id")).thenReturn(Optional.of(RandomPlayer.PLAYERID_1.toString()));

		// TODO Check the output is empty
		playersSearchHandler.listPlayers(request).block();
	}
}
