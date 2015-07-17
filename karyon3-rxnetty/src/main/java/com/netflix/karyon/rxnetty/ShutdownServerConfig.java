package com.netflix.karyon.rxnetty;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;

@Configuration(prefix="karyon.server.shutdown")
public interface ShutdownServerConfig extends ServerConfig {
    @DefaultValue("8081")
    public Integer getServerPort();
}
