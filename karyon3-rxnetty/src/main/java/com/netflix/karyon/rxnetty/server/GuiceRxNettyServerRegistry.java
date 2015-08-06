package com.netflix.karyon.rxnetty.server;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.karyon.http.DefaultServer;
import com.netflix.karyon.http.ServerConfig;

/**
 * Implementation of RxNettyHttpServerRegistry which derives the list of servers from Guice
 * bindings.  
 * 
 * @author elandau
 */
@Singleton
public class GuiceRxNettyServerRegistry implements RxNettyHttpServerRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(GuiceRxNettyServerRegistry.class);
    
    final Map<String, Provider<HttpServer<ByteBuf, ByteBuf>>> servers = new HashMap<>();
    final Map<String, ServerConfig> configs = new HashMap<>();

    final TypeLiteral<Set<HttpEndpointDefinition>> TL_ENDPOINT_SET = new TypeLiteral<Set<HttpEndpointDefinition>>() {};
    final TypeLiteral<HttpServer<ByteBuf, ByteBuf>> TL_HTTP_SERVER = new TypeLiteral<HttpServer<ByteBuf, ByteBuf>>() {};
    final TypeLiteral<ServerConfig> TL_HTTP_SERVER_CONFIG = TypeLiteral.get(ServerConfig.class);
    
    @Inject
    public GuiceRxNettyServerRegistry(Injector injector, ConfigProxyFactory configProxyFactory) {
        /**
         * Look for explicitly bound HttpServer instances.
         */
        List<Binding<HttpServer<ByteBuf, ByteBuf>>> serverBindings = injector.findBindingsByType(new TypeLiteral<HttpServer<ByteBuf, ByteBuf>>() {});
        for (Binding<HttpServer<ByteBuf, ByteBuf>> binding : serverBindings) {
            LOG.info("Found binding for server : " + binding.getKey());
            servers.put(
                    binding.getKey().getAnnotationType() != null ? binding.getKey().getAnnotationType().getName() : DefaultServer.class.getName(), 
                    binding.getProvider());
        }
        
        /**
         * Look for explicitly bound ServerConfig.  These are used either as the ServerConfig when auto-creating a server
         * or for admin status purposes.  An explicitly bound HttpServer may inject it's own configuration in which case 
         * a ServerConfig may not show up in this list
         */
        List<Binding<ServerConfig>> configBindings = injector.findBindingsByType(TypeLiteral.get(ServerConfig.class));
        for (Binding<ServerConfig> binding : configBindings) {
            LOG.info("Found binding for server config : " + binding.getKey());
            
            configs.put(
                    binding.getKey().getAnnotationType() != null ? binding.getKey().getAnnotationType().getName() : DefaultServer.class.getName(), 
                    binding.getProvider().get());
        }
        
        /**
         * Look for any qualified multibindings and auto create an HttpServer<ByteBuf, ByteBuf> to route to them using the
         * HttpRoutingRequestHandler.  Note that this behavior can be overwritten simply by adding a binding
         * to the qualified HttpServer<ByteBuf, ByteBuf>
         */
        final List<Binding<Set<HttpEndpointDefinition>>> sets = injector.findBindingsByType(TL_ENDPOINT_SET);
        for (final Binding<Set<HttpEndpointDefinition>> binding : sets) {
            final Class<? extends Annotation> qualifier = binding.getKey().getAnnotationType();
            if (qualifier != null) {
                if (!servers.containsKey(qualifier.getName())) {
                    LOG.info("Auto creating server for key : " + binding.getKey());
                    
                    final ServerConfig config = configs.containsKey(qualifier.getName()) 
                                              ? configs.get(qualifier.getName()) 
                                              : configProxyFactory.newProxy(ServerConfig.class, "karyon.httpserver." + qualifier.getSimpleName());
                    servers.put(qualifier.getName(), new Provider<HttpServer<ByteBuf, ByteBuf>>() {
                        @Override
                        public HttpServer<ByteBuf, ByteBuf> get() {
                            try {
                                return RxNetty.createHttpServer(config.getServerPort(), new HttpRoutingRequestHandler(binding.getProvider().get()));
                            } catch (Exception e) {
                                throw new ProvisionException("Error creating server " + qualifier.getCanonicalName(), e);
                            }
                        }
                    });
                }
            }
        }
    }
    
    @Override
    public Map<String, Provider<HttpServer<ByteBuf, ByteBuf>>> getServers() {
        return servers;
    }

    @Override
    public Map<String, ServerConfig> getServerConfigs() {
        return configs;
    }

}
