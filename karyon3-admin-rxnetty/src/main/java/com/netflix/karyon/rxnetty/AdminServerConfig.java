package com.netflix.karyon.rxnetty;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;

@Configuration(prefix="karyon.rxnetty.admin")
public interface AdminServerConfig extends ServerConfig {
    @DefaultValue("8077")
    public Integer getServerPort();
}
