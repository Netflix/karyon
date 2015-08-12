package com.netflix.karyon.eureka;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;

@Configuration(prefix="karyon.eureka.health")
public interface HealthCheckConfiguration {
    @DefaultValue("10")
    int getTimeout();
}
