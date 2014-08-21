package com.netflix.karyon.experimental;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.util.Types;
import com.netflix.karyon.transport.http.HttpRequestRouter;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.server.RxServer;

/**
 * @author Tomasz Bak
 */
public abstract class ExpHttpModule<I, O> extends ExpServerModule<I, O> {


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
        MapBinder.newMapBinder(binder(), String.class, RxServer.class).addBinding(nameAnnotation.value()).toProvider(
                new HttpRxServerProvider<I, O, HttpServer<I, O>>(nameAnnotation.value())
        ).asEagerSingleton();
    }

    protected LinkedBindingBuilder<HttpRequestRouter> bindRouter() {
        return bind(routerKey);
    }
}
