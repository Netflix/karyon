package com.netflix.karyon.rxnetty;

import io.reactivex.netty.protocol.http.server.HttpServer;

import java.util.Map;

import javax.inject.Provider;

public interface RxNettyServerRegistry {
    Map<String, Provider<HttpServer>> getServers();

    Map<String, ServerConfig> getServerConfigs();
}
