package com.netflix.karyon.admin.rest;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;
import com.netflix.karyon.admin.HttpServerConfig;

@Configuration(prefix="karyon.server.admin")
public interface AdminServerConfig extends HttpServerConfig {
    /**
     *  Overrides HttpServer Config
     */
    @DefaultValue("Admin")
    String name();
    
    @DefaultValue("8077")
    int port();
    
    /**
     * Admin specific settings
     */
    @DefaultValue("http://${@hostname}:8078/index.html#/${@hostname}:8077/info")
    String remoteServer();
    
    @DefaultValue("${@hostname}")
    String localServer();

    @DefaultValue("*")
    String accessControlAllowOrigin();
    
    @DefaultValue("true")
    Boolean prettyPrint();
}
