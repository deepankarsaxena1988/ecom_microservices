package ecom.apiGateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
public class RateLimitConfiguration {

	@Bean
	KeyResolver scopedClientIpKeyResolver() {
		return exchange -> {
			var remoteAddress = exchange.getRequest().getRemoteAddress();
			if (remoteAddress == null || remoteAddress.getAddress() == null) {
				return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Cannot determine client IP address"));
			}
			Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
			if (route == null) {
				return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
						"Cannot determine rate-limit route"));
			}
			Object configuredScope = route.getMetadata().get("rate-limit-scope");
			String scope = route.getId();
			if (configuredScope != null) {
				if (!(configuredScope instanceof String value) || value.isBlank()
						|| !value.matches("[A-Za-z0-9._-]+")) {
					return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
							"Invalid rate-limit-scope route metadata"));
				}
				scope = value;
			}
			return Mono.just(scope + ":" + remoteAddress.getAddress().getHostAddress());
		};
	}

	@Bean
	RedisRateLimiter redisRateLimiter() {
		return new FailClosedRedisRateLimiter();
	}
}
