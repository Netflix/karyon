package com.netflix.karyon.rxnetty.server;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServer;

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RxNettyHttpServerStarter {
    private static final Logger LOG = LoggerFactory.getLogger(RxNettyHttpServerStarter.class);
    
    @Inject
    RxNettyHttpServerStarter(RxNettyHttpServerRegistry registry) {
        for (Entry<String, Provider<HttpServer<ByteBuf, ByteBuf>>> entry : registry.getServers().entrySet()) {
            LOG.info("Starting HttpServer '{}'", entry.getKey());
            HttpServer<ByteBuf, ByteBuf> server = entry.getValue().get();
            server.start();
            LOG.info("Started HttpServer '{}' on port {}", entry.getKey(), server.getServerPort());
        }
    }
}
