package com.hms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Redis Session Configuration for Amazon ElastiCache.
 *
 * <p>Migrates all HTTP session state from in-memory server-side storage to
 * Amazon ElastiCache for Redis using Spring Session, enabling stateless
 * application instances with centralized, distributed session management.
 *
 * <p>This satisfies the cr-java-0065 (HTTP Session State Storage) cloud
 * readiness requirement: the existing {@code HttpSession} API used throughout
 * the servlet layer is preserved unchanged; Spring Session transparently
 * replaces the backing store with Redis via the
 * {@code springSessionRepositoryFilter} registered in {@code web.xml}.
 *
 * <p>Required environment variables:
 * <ul>
 *   <li>{@code REDIS_HOST}  – ElastiCache primary endpoint (default: localhost)</li>
 *   <li>{@code REDIS_PORT}  – ElastiCache port (default: 6379)</li>
 *   <li>{@code REDIS_PASSWORD} – ElastiCache auth token (optional, leave blank if
 *       in-transit encryption / AUTH is not enabled)</li>
 * </ul>
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisSessionConfig {

    @Value("${REDIS_HOST:localhost}")
    private String redisHost;

    @Value("${REDIS_PORT:6379}")
    private int redisPort;

    @Value("${REDIS_PASSWORD:}")
    private String redisPassword;

    /**
     * Creates a Lettuce-based Redis connection factory pointing at the
     * Amazon ElastiCache endpoint supplied via environment variables.
     *
     * @return configured {@link LettuceConnectionFactory}
     */
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }
}
