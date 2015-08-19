package com.netflix.karyon.admin;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;

@Configuration(prefix="karyon.server.admin")
public interface HttpServerConfig {
    String name();
    
    @DefaultValue("1")
    int threads();

    int port();

    @DefaultValue("0")
    int backlog();

    @DefaultValue("0")
    int shutdownDelay();

    @DefaultValue("true")
    boolean enabled();
}
