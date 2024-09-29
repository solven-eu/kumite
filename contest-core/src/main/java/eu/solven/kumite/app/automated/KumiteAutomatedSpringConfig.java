package eu.solven.kumite.app.automated;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.app.InjectDefaultGamesConfig;
import eu.solven.kumite.app.InjectKumiteAccountsConfig;
import eu.solven.kumite.randomgamer.RandomPlaysVs1Config;
import eu.solven.kumite.randomgamer.RandomPlaysVsThemselvesConfig;

/**
 * Each of the configuration referred here would be activated in presence of a specific Spring profile.
 * 
 * @author Benoit Lacelle
 *
 */
@Configuration
@Import({

		InjectDefaultGamesConfig.class,
		InjectKumiteAccountsConfig.class,

		RandomPlaysVs1Config.class,
		RandomPlaysVsThemselvesConfig.class,

})
public class KumiteAutomatedSpringConfig {

}
