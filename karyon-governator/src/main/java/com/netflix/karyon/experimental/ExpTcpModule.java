package com.netflix.karyon.experimental;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.util.Types;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.server.RxServer;

/**
 * @author Tomasz Bak
 */
public abstract class ExpTcpModule<I, O> extends ExpServerModule<I, O> {


    protected final Key<ConnectionHandler> connectionHandlerKey;
    protected final Key<RxServer<I, O>> serverKey;


    protected ExpTcpModule(String moduleName, Class<I> iType, Class<O> oType) {
        super(moduleName);

        connectionHandlerKey = Key.get(ConnectionHandler.class, nameAnnotation);

        TypeLiteral<RxServer<I, O>> serverTypeLiteral =
                (TypeLiteral<RxServer<I, O>>) TypeLiteral.get(Types.newParameterizedType(RxServer.class, iType, oType));
        serverKey = Key.get(serverTypeLiteral, nameAnnotation);
    }

    @Override
    protected void configure() {
        configureServer();
        MapBinder.newMapBinder(binder(), String.class, RxServer.class).addBinding(nameAnnotation.value()).toProvider(
                new TcpRxServerProvider<I, O, RxServer<I, O>>(nameAnnotation.value())
        ).asEagerSingleton();

    }

    public LinkedBindingBuilder<ConnectionHandler> bindConnectionHandler() {
        return bind(connectionHandlerKey);
    }
}
