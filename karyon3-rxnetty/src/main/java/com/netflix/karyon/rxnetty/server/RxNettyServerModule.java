package com.netflix.karyon.rxnetty.server;

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
 * <code>
    new RxNettyServerModule() {
        {@literal @}Override
        protected void configureEndpoints() {
            serve("/foo").with(FooRequestHandler.class);
            serve("*.html", "*.ico").with(StaticFileRequestHandler.class);
        }
    }
 * </code>
 * 
 * To support multiple servers (i.e ports) simply qualify the serve() statements with a standard DI Qualifier.
 * By default Karyon will construct an HttpServer and ServerConfig using the same Qualifier and associate
 * the server with these routes.  
 * 
 * <code>
    new RxNettyServerModule() {
        {@literal @}Override
        protected void configureEndpoints() {
            serve(MyServerName.class, "/foo").with(FooRequestHandler.class);
        }
   }
 * </code>
 * 
 * By default configuration for the server will be associate with the prefix: karyon.httpserver.{SimpleQualifierName}.
 * Where SimpleQulifierName is the simple class name of the qualifier.  Alternatively a custom configuration may
 * be constructed using the binding
 * 
 * <code>
    new AbstractModule() {
        {@literal @}Provides
        {@literal @}Singleton
        {@literal @}MyServerQualifier
        public ServerConfig getServerConfig() { 
            return ...;  // 
        }
    }
 * </code>
 *  
 * By default Kayron will configure a plain RxNetty HttpServer{@literal <}ByteBuf, ByteBuf{@literal >}.  To construct a server with custom 
 * configuration simply create a binding for the qualified server (note that the default server has an implicit qualifier
 * of DefaultServer.class)
 * 
 * <code>
    new AbstractModule() {
        {@literal @}Provides
        {@literal @}Singleton
        {@literal @}MyServerQualifier
        HttpServer{@literal <}ByteBuf, ByteBuf{@literal >} getShutdownServer({@literal @}MyServerQualifier ServerConfig config, final {@literal @}MyServerQualifier Set{@literal <}HttpEndpointDefinition{@literal >} defs) {
            return RxNetty.newHttpServerBuilder(
                config.getServerPort(), 
                new HttpRoutingRequestHandler(def)
                )
                .build();
        }
    }
 * </code>
 * 
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

    public static interface EndpointKeyBindingBuilder {
        void with(Class<? extends RequestHandler<ByteBuf, ByteBuf>> endpointKey);
        void with(Key<? extends RequestHandler<ByteBuf, ByteBuf>> endpointKey);
        void with(RequestHandler<ByteBuf, ByteBuf> endpoint);
    }
}
