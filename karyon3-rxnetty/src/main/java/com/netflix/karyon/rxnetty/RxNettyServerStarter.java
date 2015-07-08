package com.netflix.karyon.rxnetty;

import io.reactivex.netty.protocol.http.server.HttpServer;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

@Singleton
public class RxNettyServerStarter {
    private static final Logger LOG = LoggerFactory.getLogger(RxNettyServerStarter.class);
    
    @Inject
    RxNettyServerStarter(Injector injector) {
        List<Binding<HttpServer>> bindings = injector.findBindingsByType(TypeLiteral.get(HttpServer.class));
        for (Binding<HttpServer> binding : bindings) {
            LOG.info("Starting HttpServer '{}'", binding.getKey().getAnnotation() != null ? binding.getKey().getAnnotation() : "default");
            HttpServer server = binding.getProvider().get();
            LOG.info("Started HttpServer '{}' on port {}", binding.getKey().getAnnotation() != null ? binding.getKey().getAnnotation() : "default", server.getServerPort());
        }
    }
}
