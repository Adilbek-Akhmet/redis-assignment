package adilbek.redisassignment;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;

@Slf4j
@Configuration
public class LimitIp {

    @Bean
    public KeyResolver keyResolver() {
        return exchange -> {
            XForwardedRemoteAddressResolver resolver = XForwardedRemoteAddressResolver.maxTrustedIndex(1);
            InetSocketAddress inetSocketAddress = resolver.resolve(exchange);
            log.info("inetSocketAddress {}", inetSocketAddress);
            log.info(".getHostName() {}", inetSocketAddress.getHostName());
            return Mono.just(inetSocketAddress.getHostName());
        };
    }

    public void init() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMillis(1))
                .limitForPeriod(10)
                .timeoutDuration(Duration.ofMillis(25))
                .build();

// Create registry
        RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);

// Use registry
        RateLimiter rateLimiterWithDefaultConfig = rateLimiterRegistry
                .rateLimiter("name1");
    }
}
