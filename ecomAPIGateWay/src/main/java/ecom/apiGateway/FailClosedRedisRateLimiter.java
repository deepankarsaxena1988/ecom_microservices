package ecom.apiGateway;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FailClosedRedisRateLimiter extends RedisRateLimiter {

	private static final Logger log = LoggerFactory.getLogger(FailClosedRedisRateLimiter.class);

	public FailClosedRedisRateLimiter() {
		super(10, 20, 1);
	}

	@Override
	public Map<String, String> getHeaders(Config config, Long tokensLeft) {
		// RedisRateLimiter otherwise fails open, reporting -1 remaining tokens.
		if (tokensLeft < 0) {
			log.error("Redis is unavailable; rejecting rate-limited request");
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Rate limiter is unavailable");
		}
		return super.getHeaders(config, tokensLeft);
	}
}
