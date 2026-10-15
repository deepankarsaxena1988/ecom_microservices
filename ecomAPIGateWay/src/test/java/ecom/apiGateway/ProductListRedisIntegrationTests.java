package ecom.apiGateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@EnabledIfEnvironmentVariable(named = "REDIS_INTEGRATION_PORT", matches = "\\d+")
class ProductListRedisIntegrationTests {

	private static LettuceConnectionFactory connectionFactory;
	private static ReactiveStringRedisTemplate redis;
	private static DefaultRedisScript<List> script;
	private static StaticApplicationContext context;

	@BeforeAll
	static void connect() {
		var configuration = new RedisStandaloneConfiguration("127.0.0.1",
				Integer.parseInt(System.getenv("REDIS_INTEGRATION_PORT")));
		connectionFactory = new LettuceConnectionFactory(configuration);
		connectionFactory.afterPropertiesSet();
		connectionFactory.start();
		redis = new ReactiveStringRedisTemplate(connectionFactory);
		script = new DefaultRedisScript<>();
		script.setLocation(new ClassPathResource("META-INF/scripts/request_rate_limiter.lua"));
		script.setResultType(List.class);
		context = new StaticApplicationContext();
		context.getBeanFactory().registerSingleton("redisTemplate", redis);
		context.getBeanFactory().registerSingleton(RedisRateLimiter.REDIS_SCRIPT_NAME, script);
		context.refresh();
	}

	@AfterAll
	static void close() {
		context.close();
		connectionFactory.destroy();
	}

	@Test
	void exactBurstRefillAndSeparateClientBucketsWorkOnClusterEnabledRedis() {
		String first = "test-" + UUID.randomUUID();
		String second = "test-" + UUID.randomUUID();
		try {
			for (int remaining = 19; remaining >= 0; remaining--) {
				assertThat(consumeAt(first, 100)).isEqualTo(List.of(1L, (long) remaining));
			}
			assertThat(consumeAt(first, 100)).isEqualTo(List.of(0L, 0L));
			assertThat(consumeAt(second, 100)).isEqualTo(List.of(1L, 19L));
			assertThat(consumeAt(first, 101)).isEqualTo(List.of(1L, 9L));
			assertThat(consumeAt(first, 200)).isEqualTo(List.of(1L, 19L));
		}
		finally {
			deleteBucket(first);
			deleteBucket(second);
		}
	}

	@Test
	void separateGatewayLimiterInstancesShareRedisBucket() {
		String client = "test-" + UUID.randomUUID();
		var first = new FailClosedRedisRateLimiter();
		var second = new FailClosedRedisRateLimiter();
		first.setApplicationContext(context);
		second.setApplicationContext(context);
		try {
			assertThat(first.isAllowed("ECOMPRODUCTLIST", client).block(Duration.ofSeconds(5))
					.getHeaders()).containsEntry("X-RateLimit-Remaining", "19");
			redis.opsForValue().set(keys(client).get(1),
					Long.toString(System.currentTimeMillis() / 1000 + 60), Duration.ofSeconds(4))
					.block(Duration.ofSeconds(5));
			assertThat(second.isAllowed("ECOMPRODUCTLIST", client).block(Duration.ofSeconds(5))
					.getHeaders()).containsEntry("X-RateLimit-Remaining", "18");
		}
		finally {
			deleteBucket(client);
		}
	}

	private List<?> consumeAt(String client, long time) {
		return redis.execute(script, keys(client), List.of("10", "20", Long.toString(time), "1"))
				.blockFirst(Duration.ofSeconds(5));
	}

	private List<String> keys(String client) {
		return List.of("request_rate_limiter.{" + client + "}.tokens",
				"request_rate_limiter.{" + client + "}.timestamp");
	}

	private void deleteBucket(String client) {
		redis.delete(keys(client).toArray(String[]::new)).block(Duration.ofSeconds(5));
	}
}
