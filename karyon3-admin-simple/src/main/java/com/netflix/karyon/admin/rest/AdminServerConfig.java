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
    @DefaultValue("http://${karyon.publicHostname}:8078/index.html#/${karyon.publicHostname}:8077/appinfo")
    String remoteServer();
    
    @DefaultValue("${karyon.hostname}")
    String localServer();

    @DefaultValue("*")
    String accessControlAllowOrigin();
    
    @DefaultValue("true")
    Boolean prettyPrint();
}
