package com.netflix.karyon.admin;

import java.util.Map;

import javax.inject.Provider;

public interface HttpServerRegistry {
    Map<String, Provider<SimpleHttpServer>> getServers();

    Map<String, HttpServerConfig> getServerConfigs();
}
