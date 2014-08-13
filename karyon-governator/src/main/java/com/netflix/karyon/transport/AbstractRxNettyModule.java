package com.netflix.karyon.transport;

import java.lang.reflect.ParameterizedType;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.server.ConnectionBasedServerBuilder;
import io.reactivex.netty.server.ServerMetricsEvent;

/**
 * @author Tomasz Bak
 */
public abstract class AbstractRxNettyModule<I, O, B extends ConnectionBasedServerBuilder<I, O, B>,
        M extends ServerMetricsEvent<? extends Enum<?>>> extends AbstractModule {

    private final Class<I> iType;
    private final Class<O> oType;
    private final ParameterizedType builderType;
    private final ParameterizedType bootstrapType;
    private final ParameterizedType metricsListenerFactoryType;
    private final ParameterizedType connectionHandlerType;

    protected AbstractRxNettyModule(Class<I> iType, Class<O> oType,
                                    ParameterizedType builderType,
                                    ParameterizedType bootstrapType,
                                    ParameterizedType metricsListenerFactoryType,
                                    ParameterizedType connectionHandlerType) {
        this.iType = iType;
        this.oType = oType;
        this.builderType = builderType;
        this.bootstrapType = bootstrapType;
        this.metricsListenerFactoryType = metricsListenerFactoryType;
        this.connectionHandlerType = connectionHandlerType;
    }

    public abstract int serverPort();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected void configure() {
        int listenPort = serverPort();

        LazyDelegateConnectionHandler<I, O> lazyDelegateConnectionHandler = new LazyDelegateConnectionHandler<I, O>();
        B serverBuilder = newServerBuilder(listenPort, lazyDelegateConnectionHandler);

        ParameterizedType lazyDelegateType = Types.newParameterizedType(LazyDelegateConnectionHandler.class, iType, oType);
        TypeLiteral<LazyDelegateConnectionHandler<I, O>> lazyDelegateTypeLiteral = (TypeLiteral<LazyDelegateConnectionHandler<I, O>>) TypeLiteral.get(lazyDelegateType);
        bind(lazyDelegateTypeLiteral).toInstance(lazyDelegateConnectionHandler);

        configureEventLoops(serverBuilder);
        configureChannelOptions(serverBuilder);
        configurePipeline(serverBuilder);

        LogLevel logLevel = enableWireLogging();
        if (null != logLevel) {
            serverBuilder.enableWireLogging(logLevel);
        }

        ParameterizedType type = Types.newParameterizedType(ConnectionHandler.class, iType, oType);
        TypeLiteral<ConnectionHandler<I, O>> baseConnectionHandleTypeLiteral = (TypeLiteral<ConnectionHandler<I, O>>) TypeLiteral.get(type);
        bind(baseConnectionHandleTypeLiteral).to((TypeLiteral<? extends ConnectionHandler<I, O>>) TypeLiteral.get(connectionHandlerType));

        TypeLiteral<AbstractRxServerBootstrap<I, O, B, M>> bootstrapTypeLiteral = (TypeLiteral<AbstractRxServerBootstrap<I, O, B, M>>) TypeLiteral.get(bootstrapType);
        bind(bootstrapTypeLiteral).asEagerSingleton();

        TypeLiteral<B> builderTypeLiteral = (TypeLiteral<B>) TypeLiteral.get(builderType);
        bind(builderTypeLiteral).toInstance(serverBuilder);

        TypeLiteral<MetricEventsListenerFactory> matricsListenerTypeLiteral = (TypeLiteral<MetricEventsListenerFactory>) TypeLiteral.get(metricsListenerFactoryType);
        bind(matricsListenerTypeLiteral).toInstance(metricsEventsListenerFactory());
    }

    public abstract MetricEventsListenerFactory<I, O, M> metricsEventsListenerFactory();

    protected abstract B newServerBuilder(int port, ConnectionHandler<I, O> connectionHandler);

    public void configureChannelOptions(@SuppressWarnings("unused") B serverBuilder) {
        // No Op by default.
    }

    public void configureEventLoops(@SuppressWarnings("unused") B serverBuilder) {
        // No Op by default.
    }

    public void configurePipeline(@SuppressWarnings("unused") B serverBuilder) {
        // No Op by default.
    }

    public LogLevel enableWireLogging() {
        return LogLevel.DEBUG; // Add the wire logging handler by default so that it can be turned on at runtime.
    }

}
