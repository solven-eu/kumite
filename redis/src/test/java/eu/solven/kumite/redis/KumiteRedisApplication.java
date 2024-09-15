package eu.solven.kumite.redis;

import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication(scanBasePackages = "none")
@Import({

		// RedisConfiguration.class,

		RedisAutoConfiguration.class

})
public class KumiteRedisApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(KumiteRedisApplication.class);
		springApplication.setAdditionalProfiles("redis");
		springApplication.run();
	}

	@Bean
	public Void testInteraction(RedisTemplate<Object, Object> redisTemplate) {
		UUID someUuid = UUID.randomUUID();
		redisTemplate.opsForValue().set(someUuid, "Youpi");
		// redisTemplate.opsForSet().set(someUuid, "Youpi");
		// redisTemplate.boundSetOps("").
		redisTemplate.delete(someUuid);
		return null;
	}
}
