package eu.solven.kumite.account;

import java.util.UUID;

import reactor.core.publisher.Mono;

/**
 * Give access to the authenticated user.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteUserContextHolder {
	Mono<UUID> authenticatedAccountId();
}
