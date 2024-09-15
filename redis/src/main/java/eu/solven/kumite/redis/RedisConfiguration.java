package eu.solven.kumite.redis;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfiguration {

	@Bean
	public JedisConnectionFactory redisConnectionFactory() {
		// https://app.redislabs.com/#/databases/12516930/subscription/2419490/view-bdb/configuration
		RedisStandaloneConfiguration config =
				new RedisStandaloneConfiguration("redis-10694.c242.eu-west-1-2.ec2.redns.redis-cloud.com", 10694);
		return new JedisConnectionFactory(config);
	}

	@Bean
	public RedisTemplate<UUID, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<UUID, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}
}