package com.netflix.karyon.admin;

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SimpleHttpServerStarter {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleHttpServerStarter.class);
    
    @Inject
    SimpleHttpServerStarter(HttpServerRegistry registry) {
        for (Entry<String, Provider<SimpleHttpServer>> entry : registry.getServers().entrySet()) {
            SimpleHttpServer server = null;
            try {
                server = entry.getValue().get();
                LOG.info("Started HttpServer '{}' on port {}", entry.getKey(), server.getServerPort());
            }
            catch (Exception e) {
                if (server != null) {
                    LOG.info("Unable to start HttpServer '{}' on port {}", entry.getKey(), server.getServerPort());
                }
                throw e;
            }
        }
    }
}
