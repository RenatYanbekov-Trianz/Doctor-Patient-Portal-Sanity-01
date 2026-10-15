package com.hms.config;

import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Registers the Spring Session filter with the servlet container.
 *
 * This initializer ensures that Spring Session intercepts all HTTP requests
 * and delegates session management to Amazon ElastiCache for Redis instead
 * of the default in-memory servlet container session storage.
 *
 * This enables stateless application instances that can be horizontally
 * scaled behind an AWS load balancer without sticky sessions.
 */
public class SpringSessionInitializer extends AbstractHttpSessionApplicationInitializer {

    public SpringSessionInitializer() {
        super(RedisSessionConfig.class);
    }
}
