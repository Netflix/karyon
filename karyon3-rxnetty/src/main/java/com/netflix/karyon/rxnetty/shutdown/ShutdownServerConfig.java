package com.netflix.karyon.rxnetty.shutdown;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;
import com.netflix.karyon.http.ServerConfig;

@Configuration(prefix="karyon.httpserver.ShutdownServer")
public interface ShutdownServerConfig extends ServerConfig {
    @DefaultValue("8081")
    public Integer getServerPort();
}
