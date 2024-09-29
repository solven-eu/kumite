package eu.solven.kumite.app;

/**
 * The various Spring profiles used by this application.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteSpringProfiles {
	//
	String P_SERVER = "server";

	// The default profile, activated when no other profile is defined. Typically useful for local runs.
	String P_DEFAULT = "default";
	// This will provide reasonable default for a fast+non_prod run
	String P_DEFAULT_SERVER = "default_server";
	// If true, we install at startup a small bunch of games
	String P_INJECT_DEFAULT_GAMES = "inject_default_games";
	// If true, random players will join until leaving 1 slot for a not random player, and plays randomly
	String P_RANDOM_PLAYS_VS1 = "randomplayers_play_vs1";
	// If true, random players will join until leaving 1 slot for a not random player, and plays randomly
	String P_RANDOM_PLAYS_VSTHEMSELVES = "randomplayers_play_vsthemselves";

	// Activates the whole unsafe configuration
	String P_UNSAFE = "unsafe";
	// If true, we bypass the User login in the UI (i.e. the external-OAuth2 step required to produce account+player
	// tokens)
	String P_FAKEUSER = "fakeuser";
	// `fake_player` will enable relying on the fakePlayer but it will not tweat security related to
	// String P_FAKE_PLAYER = "fake_player";

	// Provides unsafe security settings (like DEV OAuth2 providers, and an unsafe JWT signingKey)
	String P_UNSAFE_SERVER = "unsafe_server";
	// Provides unsafe JWT signingKey
	String P_UNSAFE_OAUTH2 = "unsafe_oauth2";
	// if true, we rely on external but unsafe OAuth2 Identity Providers
	String P_UNSAFE_EXTERNAL_OAUTH2 = "unsafe_external_oauth2";

	// Used when deployed on Heroku.
	String P_HEROKU = "heroku";

	// String P_SECURED = "secured";

	// Opposite to devmode. Should be activated in production
	// Checks there is not a single unsafe|fake configurations activated
	String P_PRDMODE = "prdmode";

	// InMemory enables easy run but lack of persistence
	String P_INMEMORY = "inmemory";
	// Redis will use some Redis persistence storage
	String P_REDIS = "redis";

	// Usable by the player-application
	String P_UNSAFE_PLAYER = "unsafe_player";
}
