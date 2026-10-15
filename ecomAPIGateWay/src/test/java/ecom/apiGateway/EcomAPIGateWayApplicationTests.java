package ecom.apiGateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"eureka.client.enabled=false",
		"spring.cloud.gateway.discovery.locator.enabled=false"
})
class EcomAPIGateWayApplicationTests {

	private static final AtomicInteger backendCalls = new AtomicInteger();
	private static final DisposableServer backend = HttpServer.create().host("127.0.0.1").port(0)
			.handle((request, response) -> {
				backendCalls.incrementAndGet();
				return response.sendString(Mono.just(request.uri()));
			}).bindNow();

	@LocalServerPort
	int port;

	@MockBean
	ReactiveStringRedisTemplate redis;

	@Autowired
	RateLimitConfiguration configuration;

	private WebTestClient client;

	@DynamicPropertySource
	static void backendProperties(DynamicPropertyRegistry registry) throws IOException {
		// Indexed route lists replace rather than merge across property sources.
		Properties properties = new Properties();
		try (var input = new ClassPathResource("application.properties").getInputStream()) {
			properties.load(input);
		}
		properties.stringPropertyNames().stream()
				.filter(name -> name.startsWith("spring.cloud.gateway.routes["))
				.forEach(name -> registry.add(name, () -> properties.getProperty(name)));
		registry.add("spring.cloud.gateway.routes[0].uri", () -> "http://127.0.0.1:" + backend.port());
		registry.add("spring.cloud.gateway.routes[1].uri", () -> "http://127.0.0.1:" + backend.port());
	}

	@BeforeEach
	void setUp() {
		backendCalls.set(0);
		client = WebTestClient.bindToServer().baseUrl("http://127.0.0.1:" + port)
				.responseTimeout(Duration.ofSeconds(10)).build();
		when(redis.execute(any(), anyList(), anyList())).thenReturn(Flux.just(List.of(1L, 19L)));
	}

	@AfterAll
	static void stopBackend() {
		backend.disposeNow();
	}

	@Test
	void forwardsProductPathAndReportsConfiguredLimits() {
		client.get().uri("/ecom/productList").exchange().expectStatus().isOk()
				.expectHeader().valueEquals("X-RateLimit-Replenish-Rate", "10")
				.expectHeader().valueEquals("X-RateLimit-Burst-Capacity", "20")
				.expectHeader().valueEquals("X-RateLimit-Requested-Tokens", "1")
				.expectHeader().valueEquals("X-RateLimit-Remaining", "19")
				.expectBody(String.class).isEqualTo("/ecom/productList");
		assertThat(backendCalls.get()).isEqualTo(1);
		verify(redis).execute(any(), eq(List.of(
				"request_rate_limiter.{product-list:127.0.0.1}.tokens",
				"request_rate_limiter.{product-list:127.0.0.1}.timestamp")), anyList());
	}

	@Test
	void rejectsExcessRequestsWithoutCallingProductBackend() {
		when(redis.execute(any(), anyList(), anyList())).thenReturn(Flux.just(List.of(0L, 0L)));
		client.get().uri("/ecom/productDetail/1").exchange().expectStatus().isEqualTo(429);
		assertThat(backendCalls.get()).isZero();
	}

	@Test
	void redisFailureReturns503ButDoesNotLimitCheckout() {
		when(redis.execute(any(), anyList(), anyList()))
				.thenReturn(Flux.error(new IllegalStateException("Redis unavailable for test")));
		client.get().uri("/ecom/productList").exchange().expectStatus().isEqualTo(503);
		assertThat(backendCalls.get()).isZero();
		client.get().uri("/checkout/test").exchange().expectStatus().isOk()
				.expectHeader().doesNotExist("X-RateLimit-Remaining")
				.expectBody(String.class).isEqualTo("/checkout/test");
	}

	@Test
	void contextLoads() {
		assertThat(configuration).isNotNull();
	}
}
