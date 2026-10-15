package com.hms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.time.Duration;

/**
 * Spring Session Redis configuration for distributed session management.
 *
 * Migrates HTTP session state from in-memory server-side storage to
 * Amazon ElastiCache for Redis, enabling:
 * - Stateless application instances for horizontal scaling
 * - Centralized session management across multiple instances
 * - Session persistence across instance restarts
 * - Compatibility with AWS load balancers without sticky sessions
 *
 * Redis connection is configured via environment variables:
 * - REDIS_HOST: ElastiCache Redis endpoint (default: localhost)
 * - REDIS_PORT: Redis port (default: 6379)
 * - REDIS_PASSWORD: Redis auth token for ElastiCache (optional)
 * - SESSION_TIMEOUT_SECONDS: Session timeout in seconds (default: 1800)
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisSessionConfig {

    /**
     * Configures the Redis connection factory pointing to Amazon ElastiCache.
     * Connection parameters are read from environment variables to follow
     * 12-factor app principles (no hardcoded connection strings).
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int redisPort = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String redisPassword = System.getenv("REDIS_PASSWORD");

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        // Configure Lettuce client with explicit timeouts to prevent indefinite hangs
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(10))
                .shutdownTimeout(Duration.ofSeconds(5))
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }
}
