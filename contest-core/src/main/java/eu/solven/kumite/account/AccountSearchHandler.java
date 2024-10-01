package eu.solven.kumite.account;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.app.webflux.api.KumiteHandlerHelper;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class AccountSearchHandler {
	final KumiteUsersRegistry usersRegistry;

	public Mono<ServerResponse> searchAccounts(ServerRequest request) {
		// We shall later enabling searching by school/company/country
		UUID accountId = KumiteHandlerHelper.uuid(request, "account_id");

		Optional<KumiteUser> optUser = usersRegistry.optUser(accountId);

		List<KumiteUser> match = optUser.map(u -> Collections.singletonList(u)).orElse(Collections.emptyList());

		return KumiteHandlerHelper.okAsJson(match);
	}
}