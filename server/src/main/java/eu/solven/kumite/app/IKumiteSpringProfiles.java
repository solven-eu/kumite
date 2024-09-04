package eu.solven.kumite.app;

public interface IKumiteSpringProfiles {
	String P_INJECT_DEFAULT_GAMES = "inject_default_games";
	// If true, we bypass the User login in the UI (i.e. the external-OAuth2 step required to produce account+player
	// tokens)
	String P_DEFAULT_FAKE_USER = "fake_user";
	// If true, we bypass the account+player tokens
	String P_DEFAULT_FAKE_PLAYER = "fake_player";
}
