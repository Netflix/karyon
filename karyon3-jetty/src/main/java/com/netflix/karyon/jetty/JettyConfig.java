package com.netflix.karyon.jetty;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;

@Configuration(prefix="karyon.jetty")
public interface JettyConfig {
    @DefaultValue("8080")
    int getPort();
}
