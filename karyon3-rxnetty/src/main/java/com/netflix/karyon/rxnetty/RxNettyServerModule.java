package com.netflix.karyon.rxnetty;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.RequestHandler;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.netflix.karyon.http.DefaultServer;

/**
 * Optional module with DSL to specify routes in a module similar to the approach of specifying
 * Servlets using Guice's ServletModule.  Routes may be specified as either a regex, servlet pattern
 * or simple prefix.  
 * 
 * Example,
 * <pre>
 * {@code
    new RxNettyServerModule() {
        @Override
        protected void configureEndpoints() {
            serve("/foo").with(FooRequestHandler.class);
            serve("*.html", "*.ico").with(StaticFileRequestHandler.class);
        }
    },
 * }
 * 
 * To support multiple servers (i.e ports) simply qualify the serve() statements with a standard DI Qualifier.
 * By default Karyon will construct an HttpServer and ServerConfig using the same Qualifier and associate
 * the server with these routes.  
 * 
 * <pre>
 * {@code
    new RxNettyServerModule() {
        @Override
        protected void configureEndpoints() {
            serve(MyServerName.class, "/foo").with(FooRequestHandler.class);
        }
    },
 * }
 * </pre>
 * 
 * By default configuration for the server will be associate with the prefix: karyon.httpserver.{SimpleQualifierName}.
 * Where SimpleQulifierName is the simple class name of the qualifier.  Alternatively a custom configuration may
 * be constructed using the binding
 * 
 * <pre>
 * {@code
    new AbstractModule() {
        @Provides
        @Singleton
        @MyServerQualifier
        public ServerConfig getServerConfig() { 
            return ...;  // 
        }
    }
 * }
 * </pre>
 *  
 * By default Kayron will configure a plain RxNetty HttpServer<ByteBuf, ByteBuf>.  To construct a server with custom 
 * configuration simply create a binding for the qualified server (note that the default server has an implicit qualifier
 * of DefaultServer.class)
 * 
 * <pre>
 * {@code
 *  
    new AbstractModule() {
        @Provides
        @Singleton
        @MyServerQualifier
        HttpServer<ByteBuf, ByteBuf> getShutdownServer(@MyServerQualifier ServerConfig config, final @MyServerQualifier Set<HttpEndpointDefinition> defs) {
            return RxNetty.newHttpServerBuilder(
                config.getServerPort(), 
                new HttpRoutingRequestHandler(def)
                )
                .build();
        }
    }
 * }
 * </pre>
 * @author elandau
 *
 */
public abstract class RxNettyServerModule extends AbstractModule {
    private EndpointsModuleBuilder endpointsModuleBuilder;

    @Override
    protected void configure() {
        install(new RxNettyModule());
        
        endpointsModuleBuilder = new EndpointsModuleBuilder(binder());
        configureEndpoints();
    }
    
    protected abstract void configureEndpoints();
    
    /**
     * @param urlPattern Any Servlet-style pattern. examples: /*, /html/*, *.html, etc.
     * @since 2.0
     */
    protected final EndpointKeyBindingBuilder serve(String urlPattern, String... morePatterns) {
        List<String> patterns = new ArrayList<>();
        patterns.add(urlPattern);
        if (morePatterns != null) {
            patterns.addAll(Arrays.asList(morePatterns));
        }
        return endpointsModuleBuilder.serve(DefaultServer.class, patterns);
    }

    protected final EndpointKeyBindingBuilder serve(Class<? extends Annotation> qualifier, String urlPattern, String... morePatterns) {
        List<String> patterns = new ArrayList<>();
        patterns.add(urlPattern);
        if (morePatterns != null) {
            patterns.addAll(Arrays.asList(morePatterns));
        }
        return endpointsModuleBuilder.serve(qualifier, patterns);
    }

    /**
     * @param regex Any Java-style regular expression.
     * @since 2.0
     */
    protected final EndpointKeyBindingBuilder serveRegex(String regex, String... regexes) {
        List<String> patterns = new ArrayList<>();
        patterns.add(regex);
        if (regexes != null) {
            patterns.addAll(Arrays.asList(regexes));
        }
        return endpointsModuleBuilder.serveRegex(DefaultServer.class, patterns);
    }

    protected final EndpointKeyBindingBuilder serveRegex(Class<? extends Annotation> qualifier, String regex, String... regexes) {
        List<String> patterns = new ArrayList<>();
        patterns.add(regex);
        if (regexes != null) {
            patterns.addAll(Arrays.asList(regexes));
        }
        return endpointsModuleBuilder.serveRegex(qualifier, patterns);
    }

    /**
     * See the EDSL examples at {@link EndpointModule#configureEndpoints()}
     *
     * @since 2.0
     */
    public static interface EndpointKeyBindingBuilder {
        void with(Class<? extends RequestHandler<ByteBuf, ByteBuf>> endpointKey);
        void with(Key<? extends RequestHandler<ByteBuf, ByteBuf>> endpointKey);
        void with(RequestHandler<ByteBuf, ByteBuf> endpoint);
    }
}
