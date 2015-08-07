package com.netflix.karyon.http;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;

@Configuration(prefix="karyon.httpserver")
public interface ServerConfig {
    @DefaultValue("8080")
    public Integer getServerPort();
}
