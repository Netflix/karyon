package com.netflix.karyon.rxnetty;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;

@Configuration(prefix="karyon.rxnetty")
public interface ServerConfig {
    @DefaultValue("8080")
    public Integer getServerPort();
}
