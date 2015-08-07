package com.netflix.karyon.admin.rest;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;
import com.netflix.karyon.admin.HttpServerConfig;

@Configuration(prefix="karyon.server.admin")
public interface AdminServerConfig extends HttpServerConfig{
    @DefaultValue("Admin")
    String name();
    
    @DefaultValue("8077")
    int port();
    
    @DefaultValue("http://${@serverId}:8078/index.html?serverId=${@serverId}&resourceId=info")
    String remoteServer();
    
    @DefaultValue("${@serverId}")
    String localServer();

    @DefaultValue("*")
    String accessControlAllowOrigin();
}
