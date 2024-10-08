package eu.solven.kumite.app;

import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGenerator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KumitePlayerRandomConfiguration {

	@Bean
	RandomGenerator playerRandomGenerator(Environment env) {
		String rawSeed = env.getProperty("kumite.random.seed", "random");
		RandomGenerator r;
		if ("random".equals(rawSeed)) {
			r = new SecureRandom();
		} else {
			log.warn("Using a predictable Random");
			r = new Random(Long.parseLong(rawSeed));
		}

		return r;
	}
}
