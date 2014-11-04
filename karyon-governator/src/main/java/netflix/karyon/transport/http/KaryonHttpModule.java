package netflix.karyon.transport.http;

import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import io.reactivex.netty.server.RxServer;
import netflix.karyon.transport.AbstractServerModule;
import netflix.karyon.transport.http.KaryonHttpModule.HttpServerConfigBuilder;

import static netflix.karyon.utils.TypeUtils.keyFor;

/**
 * @author Tomasz Bak
 */
public abstract class KaryonHttpModule<I, O> extends AbstractServerModule<I, O, HttpServerConfigBuilder> {

    protected final Key<RequestHandler<I, O>> routerKey;
    protected final Key<HttpServer<I, O>> httpServerKey;
    protected final Key<GovernatorHttpInterceptorSupport<I, O>> interceptorSupportKey;

    private final GovernatorHttpInterceptorSupport<I, O> interceptorSupportInstance = new GovernatorHttpInterceptorSupport<I, O>();

    protected KaryonHttpModule(String moduleName, Class<I> iType, Class<O> oType) {
        super(moduleName, iType, oType);

        routerKey = keyFor(RequestHandler.class, iType, oType, nameAnnotation);
        interceptorSupportKey = keyFor(GovernatorHttpInterceptorSupport.class, iType, oType, nameAnnotation);
        httpServerKey = keyFor(HttpServer.class, iType, oType, nameAnnotation);
    }

    @Override
    protected void configure() {
        configureServer();

        bind(serverConfigKey).toInstance(serverConfigBuilder.build());
        bind(interceptorSupportKey).toInstance(interceptorSupportInstance);

        MapBinder.newMapBinder(binder(), String.class, RxServer.class).addBinding(nameAnnotation.value()).toProvider(
                new HttpRxServerProvider<I, O, HttpServer<I, O>>(nameAnnotation.value(), iType, oType)
        ).asEagerSingleton();
    }

    @Override
    protected HttpServerConfigBuilder newServerConfigBuilder() {
        return new HttpServerConfigBuilder();
    }

    protected LinkedBindingBuilder<RequestHandler<I, O>> bindRouter() {
        return bind(routerKey);
    }

    protected GovernatorHttpInterceptorSupport<I, O> interceptorSupport() {
        return interceptorSupportInstance;
    }

    public static class HttpServerConfig extends AbstractServerModule.ServerConfig {

        private final int threadPoolSize;

        public HttpServerConfig(int port) {
            super(port);
            this.threadPoolSize = -1;
        }

        public HttpServerConfig(int port, int threadPoolSize) {
            super(port);
            this.threadPoolSize = threadPoolSize;
        }

        public int getThreadPoolSize() {
            return threadPoolSize;
        }

        public boolean requiresThreadPool() {
            return threadPoolSize > 0;
        }
    }

    public static class HttpServerConfigBuilder
            extends AbstractServerModule.ServerConfigBuilder<HttpServerConfigBuilder, HttpServerConfig> {

        protected int poolSize = -1;

        public HttpServerConfigBuilder threadPoolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        @Override
        public HttpServerConfig build() {
            return new HttpServerConfig(port, poolSize);
        }
    }
}
