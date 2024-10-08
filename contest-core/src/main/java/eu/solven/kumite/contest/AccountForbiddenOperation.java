package eu.solven.kumite.contest;

import eu.solven.kumite.account.internal.KumiteUser;

/**
 * When an {@link KumiteUser} tries to do a forbidden operation.
 * 
 * @author Benoit Lacelle
 *
 */
public class AccountForbiddenOperation extends IllegalArgumentException {
	private static final long serialVersionUID = -1332927839258276261L;

	public AccountForbiddenOperation(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountForbiddenOperation(String s) {
		super(s);
	}

}
