package com.netflix.karyon.experimental;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.reactivex.netty.protocol.http.server.HttpServer;

/**
 * @author Tomasz Bak
 */
public abstract class ExpHttpModule<I, O> extends ExpServerModule {


    protected final Key<HttpRequestRouter> routerKey;
    protected final Key<HttpServer<I, O>> httpServerKey;


    protected ExpHttpModule(String moduleName, Class<I> iType, Class<O> oType) {
        super(moduleName);

        routerKey = Key.get(HttpRequestRouter.class, nameAnnotation);

        TypeLiteral<HttpServer<I, O>> httpServerTypeLiteral = (TypeLiteral<HttpServer<I, O>>) TypeLiteral.get(Types.newParameterizedType(HttpServer.class, iType, oType));
        httpServerKey = Key.get(httpServerTypeLiteral, nameAnnotation);
    }

    @Override
    protected void configure() {
        configureServer();
        bind(httpServerKey).toProvider(new HttpRxServerProvider(nameAnnotation.value()));
    }

    protected abstract void configureServer();

    protected void bindRouter(Class<? extends HttpRequestRouter<I, O>> routerClass) {
        bind(routerKey).to(routerClass);
    }
}
