package com.netflix.karyon.transport;

import java.lang.reflect.ParameterizedType;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.netty.handler.logging.LogLevel;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.server.ConnectionBasedServerBuilder;
import io.reactivex.netty.server.ServerMetricsEvent;
import rx.Observable;

/**
 * @author Tomasz Bak
 */
public abstract class AbstractRxNettyModule<I, O, B extends ConnectionBasedServerBuilder<I, O, B>,
        M extends ServerMetricsEvent<? extends Enum<?>>> extends AbstractModule {

    private final ParameterizedType builderType;
    private final ParameterizedType bootstrapType;
    private final ParameterizedType metricsListenerFactoryType;

    protected AbstractRxNettyModule(ParameterizedType builderType,
                                    ParameterizedType bootstrapType,
                                    ParameterizedType metricsListenerFactoryType) {
        this.builderType = builderType;
        this.bootstrapType = bootstrapType;
        this.metricsListenerFactoryType = metricsListenerFactoryType;
    }

    @Override
    protected void configure() {
        int listenPort = serverPort();
        int shutdownPort = shutdownPort();

        bind(Ports.class).toInstance(new Ports(listenPort, shutdownPort));

        B serverBuilder = newServerBuilder(listenPort, connectionHandler());

        configureEventLoops(serverBuilder);
        configureChannelOptions(serverBuilder);
        configurePipeline(serverBuilder);

        LogLevel logLevel = enableWireLogging();
        if (null != logLevel) {
            serverBuilder.enableWireLogging(logLevel);
        }

        @SuppressWarnings("unchecked")
        TypeLiteral<AbstractRxServerBootstrap<I, O, B, M>> bootstrapTypeLiteral = (TypeLiteral<AbstractRxServerBootstrap<I, O, B, M>>) TypeLiteral.get(bootstrapType);
        bind(bootstrapTypeLiteral).asEagerSingleton();

        @SuppressWarnings("unchecked")
        TypeLiteral<B> builderTypeLiteral = (TypeLiteral<B>) TypeLiteral.get(builderType);
        bind(builderTypeLiteral).toInstance(serverBuilder);

        @SuppressWarnings("unchecked")
        TypeLiteral<MetricEventsListenerFactory> matricsListenerTypeLiteral = (TypeLiteral<MetricEventsListenerFactory>) TypeLiteral.get(metricsListenerFactoryType);
        bind(matricsListenerTypeLiteral).toInstance(metricsEventsListenerFactory());
    }

    public abstract int serverPort();

    public abstract int shutdownPort();

    public abstract ConnectionHandler<I, O> connectionHandler();

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

    public static class LazyDelegateConnectionHandler<I, O> implements ConnectionHandler<I, O> {
        private ConnectionHandler<I, O> connectionHandler;

        @Override
        public Observable<Void> handle(ObservableConnection<I, O> connection) {
            return connectionHandler.handle(connection);
        }

        public void setHandler(ConnectionHandler<I, O> connectionHandler) {
            this.connectionHandler = connectionHandler;
        }
    }
}
