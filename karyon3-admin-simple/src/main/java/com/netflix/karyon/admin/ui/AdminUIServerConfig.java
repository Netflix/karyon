package com.netflix.karyon.admin.ui;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.annotations.DefaultValue;
import com.netflix.karyon.admin.HttpServerConfig;

@Configuration(prefix="karyon.server.adminui")
public interface AdminUIServerConfig extends HttpServerConfig{
    @DefaultValue("AdminUI")
    String name();
    
    @DefaultValue("8078")
    int port();
    
    @DefaultValue("false")
    boolean cacheResources();
    
    @DefaultValue("admin")
    String resourcePath();
    
    @DefaultValue("/mime.types")
    String mimeTypesResourceName();

    @DefaultValue("KaryonAdmin")
    String getServerName();
}
