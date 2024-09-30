package eu.solven.kumite.redis;

import java.util.UUID;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootApplication(scanBasePackages = "none")
@Import({

		RedisAutoConfiguration.class

})
public class TestRedisUserRepository {

	@Bean
	public Void testInteraction(RedisTemplate<Object, Object> redisTemplate) {
		UUID someUuid = UUID.randomUUID();
		redisTemplate.opsForValue().set(someUuid, "Youpi");
		redisTemplate.delete(someUuid);
		return null;
	}
}
