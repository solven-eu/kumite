package eu.solven.kumite.security;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

import eu.solven.kumite.account.JwtUserContextHolder;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.webflux.KumiteWebExceptionHandler;
import eu.solven.kumite.app.webflux.api.KumiteLoginController;
import eu.solven.kumite.app.webflux.api.KumitePublicController;
import eu.solven.kumite.app.webflux.api.MetadataController;
import eu.solven.kumite.login.RefreshTokenWrapper;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import eu.solven.kumite.oauth2.resourceserver.JwtWebFluxSecurity;
import eu.solven.kumite.oauth2.resourceserver.KumiteResourceServerConfiguration;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import lombok.extern.slf4j.Slf4j;

// https://docs.spring.io/spring-security/reference/reactive/oauth2/login/advanced.html#webflux-oauth2-login-advanced-userinfo-endpoint
@EnableWebFluxSecurity
@Import({

		SocialWebFluxSecurity.class,
		JwtWebFluxSecurity.class,

		KumitePublicController.class,
		KumiteLoginController.class,
		MetadataController.class,

		KumiteResourceServerConfiguration.class,
		KumiteTokenService.class,

		JwtUserContextHolder.class,

		KumiteWebExceptionHandler.class,

})
@Slf4j
public class KumiteSecuritySpringConfig {

	@Profile(IKumiteSpringProfiles.P_PRDMODE)
	@Bean
	public Void checkSecured(Environment env) {
		boolean acceptUnsafe = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_UNSAFE,
				IKumiteSpringProfiles.P_UNSAFE_SERVER,
				IKumiteSpringProfiles.P_FAKEUSER,
				IKumiteSpringProfiles.P_UNSAFE_EXTERNAL_OAUTH2));

		if (acceptUnsafe) {
			throw new IllegalStateException("At least one unsafe profile is activated");
		}

		if ("NEEDS_TO_BE_DEFINED".equals(env.getProperty("kumite.login.oauth2.github.clientId"))) {
			log.warn("We lack a proper environment variable for XXX");
		} else if ("NEEDS_TO_BE_DEFINED".equals(env.getProperty("kumite.login.oauth2.github.clientSecret"))) {
			log.warn("We lack a proper environment variable for XXX");
		}

		return null;
	}

	@Bean
	public Void checkSpringProfilesConsistency(Environment env) {
		boolean acceptInMemory = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_INMEMORY));
		boolean acceptRedis = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_REDIS));

		if (acceptRedis && acceptInMemory) {
			throw new IllegalStateException(
					"Can not be both " + IKumiteSpringProfiles.P_INMEMORY + " and " + IKumiteSpringProfiles.P_REDIS);
		}

		return null;
	}

	// We print a refreshToken at startup, as it makes it easier to configure a player
	@Bean
	public Void printRandomPlayerRefreshToken(@Qualifier("random") KumiteUser user,
			KumiteTokenService tokenService,
			IAccountPlayersRegistry accountPlayersRegistry) {
		UUID accountId = user.getAccountId();

		Set<UUID> playerIds = accountPlayersRegistry.makeDynamicHasPlayers(accountId).getPlayerIds();
		RefreshTokenWrapper refreshToken = tokenService.wrapInJwtRefreshToken(accountId, playerIds);

		log.info("refresh_token for accountId={}: {}", user.getAccountId(), refreshToken);

		return null;
	}
}