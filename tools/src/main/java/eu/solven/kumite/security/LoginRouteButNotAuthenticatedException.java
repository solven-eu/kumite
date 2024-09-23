package eu.solven.kumite.security;

/**
 * This exception is typically used to report 401.
 * 
 * @author Benoit Lacelle
 *
 */
public class LoginRouteButNotAuthenticatedException extends RuntimeException {
	private static final long serialVersionUID = -8569851345844040153L;

	public LoginRouteButNotAuthenticatedException(String msg) {
		super(msg);
	}

}
