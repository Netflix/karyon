package com.netflix.karyon.transport.tcp;

import com.google.inject.Inject;
import com.netflix.governator.guice.lazy.FineGrainedLazySingleton;
import com.netflix.karyon.transport.AbstractRxServerBootstrap;
import com.netflix.karyon.transport.LazyDelegateConnectionHandler;
import com.netflix.karyon.transport.MetricEventsListenerFactory;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.server.ServerBuilder;
import io.reactivex.netty.server.ServerMetricsEvent;
import io.reactivex.netty.server.ServerMetricsEvent.EventType;

/**
 * @author Tomasz Bak
 */
@FineGrainedLazySingleton
public class TcpServerBootstrap<I, O> extends AbstractRxServerBootstrap<I, O, ServerBuilder<I, O>, ServerMetricsEvent<EventType>> {
    @Inject
    public TcpServerBootstrap(LazyDelegateConnectionHandler<I, O> lazyConnectionHandler,
                              ConnectionHandler<I, O> connectionHandler,
                              MetricEventsListenerFactory<I, O, ServerMetricsEvent<EventType>> metricEventsListenerFactory,
                              ServerBuilder<I, O> serverBuilder) {
        super(lazyConnectionHandler, connectionHandler, metricEventsListenerFactory, serverBuilder);
    }
}
