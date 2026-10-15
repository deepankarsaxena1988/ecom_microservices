package ecom.apiGateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;

class RateLimitConfigurationTests {

	private final RateLimitConfiguration configuration = new RateLimitConfiguration();

	@Test
	void usesSocketIpAndIgnoresSpoofedForwardingHeaders() {
		var request = MockServerHttpRequest.get("/ecom/productList")
				.remoteAddress(new InetSocketAddress("192.0.2.10", 12345))
				.header("X-Forwarded-For", "198.51.100.1")
				.header("Forwarded", "for=198.51.100.2");
		var exchange = MockServerWebExchange.from(request);
		exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route("product", "product-list"));
		assertThat(configuration.scopedClientIpKeyResolver()
				.resolve(exchange).block()).isEqualTo("product-list:192.0.2.10");
	}

	@Test
	void rejectsMissingIpRatherThanSharingAnAnonymousBucket() {
		assertThatThrownBy(() -> configuration.scopedClientIpKeyResolver()
				.resolve(MockServerWebExchange.from(MockServerHttpRequest.get("/ecom/productList"))).block())
				.isInstanceOfSatisfying(ResponseStatusException.class,
						error -> assertThat(error.getStatusCode().value()).isEqualTo(400));
	}

	@Test
	void separatesServicesButAllowsRoutesToShareAnExplicitScope() {
		assertThat(resolve(route("product", "product-list"))).isEqualTo("product-list:192.0.2.10");
		assertThat(resolve(route("checkout", "checkout"))).isEqualTo("checkout:192.0.2.10");
		assertThat(resolve(route("webhook", "checkout"))).isEqualTo(resolve(route("checkout", "checkout")));
		assertThat(resolve(route("other", null))).isEqualTo("other:192.0.2.10");
	}

	@Test
	void rejectsMissingRouteAndInvalidScope() {
		assertThatThrownBy(() -> resolve(null))
				.isInstanceOfSatisfying(ResponseStatusException.class,
						error -> assertThat(error.getStatusCode().value()).isEqualTo(500));
		for (String scope : new String[] { "", " ", "bad{scope}", "checkout:other" }) {
			assertThatThrownBy(() -> resolve(route("test", scope)))
					.isInstanceOfSatisfying(ResponseStatusException.class,
							error -> assertThat(error.getStatusCode().value()).isEqualTo(500));
		}
	}

	private Route route(String id, String scope) {
		var builder = Route.async().id(id).uri("http://localhost:9203").predicate(exchange -> true);
		if (scope != null) {
			builder.metadata("rate-limit-scope", scope);
		}
		return builder.build();
	}

	private String resolve(Route route) {
		var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test")
				.remoteAddress(new InetSocketAddress("192.0.2.10", 12345)));
		if (route != null) {
			exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);
		}
		return configuration.scopedClientIpKeyResolver().resolve(exchange).block();
	}

	@Test
	void productRouteUsesClusterIpServiceAndCannotBeBypassedByDiscovery() throws IOException {
		Properties properties = new Properties();
		try (var input = new ClassPathResource("application.properties").getInputStream()) {
			properties.load(input);
		}
		assertThat(properties.getProperty("spring.cloud.gateway.routes[0].uri"))
				.isEqualTo("http://ecom-product-list.default.svc.cluster.local:9203");
		var expression = new SpelExpressionParser().parseExpression(properties.getProperty(
				"spring.cloud.gateway.discovery.locator.include-expression"));
		for (String id : new String[] { "ECOMPRODUCTLIST", "ecomProductList" }) {
			var context = new StandardEvaluationContext(new DefaultServiceInstance("test", id, "localhost", 9203, false));
			assertThat(expression.getValue(context, Boolean.class)).isFalse();
		}
		var checkout = new DefaultServiceInstance("test", "ECOMCHECKOUT", "localhost", 8085, false);
		assertThat(expression.getValue(new StandardEvaluationContext(checkout), Boolean.class)).isTrue();
	}
}
