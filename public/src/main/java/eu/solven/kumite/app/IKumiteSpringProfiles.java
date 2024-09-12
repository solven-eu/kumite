package eu.solven.kumite.app;

public interface IKumiteSpringProfiles {
	//
	String P_SERVER = "server";

	// The default profile, activated when no other profile is defined. Typically useful for local runs.
	String P_DEFAULT = "default";
	// This will provide reasonable default for a fast+non_prod run
	String P_DEFAULT_SERVER = "default_server";
	// If true, we install at startup a small bunch of games
	String P_INJECT_DEFAULT_GAMES = "inject_default_games";

	// Activates the whole fake configuration
	String P_FAKE = "fake";
	// If true, we rely on unsafe security settings (like DEV OAuth2 providers, and an unsafe JWT signingKey)
	String P_FAKE_SERVER = "fake_server";
	// If true, we bypass the account+player tokens
	String P_FAKE_PLAYER = "fake_player";

	// If true, we bypass the User login in the UI (i.e. the external-OAuth2 step required to produce account+player
	// tokens)
	String P_FAKE_USER = "fake_user";
	// if true, we rely on external but unsafe OAuth2 Identity Providers
	String P_UNSAFE_EXTERNAL_OAUTH2 = "unsafe_external_oauth2";

	// Used when deployed on Heroku.
	String P_HEROKU = "heroku";
}
