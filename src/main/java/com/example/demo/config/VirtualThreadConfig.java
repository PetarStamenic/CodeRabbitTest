package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuration class that enables Project Loom virtual threads for the
 * application's async executor and for Tomcat's connector thread pool.
 *
 * <p>Setting {@code spring.threads.virtual.enabled=true} in
 * {@code application.properties} is the canonical way to switch Tomcat
 * and Spring MVC over to virtual threads.  The beans below expose a
 * named virtual-thread {@link Executor} that can be injected wherever
 * fine-grained control is needed.</p>
 */
@Configuration
@EnableAsync
public class VirtualThreadConfig {

    /**
     * A virtual-thread–backed {@link Executor} available for injection
     * (e.g. {@code @Async("virtualThreadExecutor")}).
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
