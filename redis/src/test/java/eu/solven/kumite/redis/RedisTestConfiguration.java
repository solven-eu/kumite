package eu.solven.kumite.redis;

import java.io.IOException;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.TestConfiguration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;

/**
 * Provides an embedded Redis for unitTests purposes.
 * 
 * @author Benoit Lacelle
 *
 */
@TestConfiguration
@Slf4j
public class RedisTestConfiguration {

	private RedisServer redisServer;

	public RedisTestConfiguration(RedisProperties redisProperties) throws IOException {
		this.redisServer = new RedisServer(redisProperties.getPort());
	}

	@PostConstruct
	public void postConstruct() throws IOException {
		log.info("Starting EmbeddedRedis");
		redisServer.start();
	}

	@PreDestroy
	public void preDestroy() throws IOException {
		log.info("Stopping EmbeddedRedis");
		redisServer.stop();
	}
}