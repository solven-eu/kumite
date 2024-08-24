package eu.solven.kumite.account;

import java.util.UUID;

import lombok.Value;

/**
 * An account refers to the identity of a human. A human may manage multiple players.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
public class KumiteAccount {
	UUID accountUuid;

	String email;
}
