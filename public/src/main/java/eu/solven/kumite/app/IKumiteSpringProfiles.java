package eu.solven.kumite.app;

public interface IKumiteSpringProfiles {
	// The default profile, activated when no other profile is defined. Typically useful for local runs.
	String P_DEFAULT = "default";
	// This will provide reasonable default for a fast+non_prod run
	String P_DEFAULT_SERVER = "default_server";
	// If true, we install at startup a small bunch of games
	String P_INJECT_DEFAULT_GAMES = "inject_default_games";
	// If true, we bypass the User login in the UI (i.e. the external-OAuth2 step required to produce account+player
	// tokens)
	String P_DEFAULT_FAKE_USER = "fake_user";
	// If true, we bypass the account+player tokens
	String P_DEFAULT_FAKE_PLAYER = "fake_player";
}
