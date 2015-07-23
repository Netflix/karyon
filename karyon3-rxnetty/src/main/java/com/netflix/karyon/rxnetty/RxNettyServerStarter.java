package com.netflix.karyon.rxnetty;

import io.reactivex.netty.protocol.http.server.HttpServer;

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RxNettyServerStarter {
    private static final Logger LOG = LoggerFactory.getLogger(RxNettyServerStarter.class);
    
    @Inject
    RxNettyServerStarter(RxNettyServerRegistry registry) {
        for (Entry<String, Provider<HttpServer>> entry : registry.getServers().entrySet()) {
            LOG.info("Starting HttpServer '{}'", entry.getKey());
            HttpServer server = entry.getValue().get();
            server.start();
            LOG.info("Started HttpServer '{}' on port {}", entry.getKey(), server.getServerPort());
        }
    }
}
