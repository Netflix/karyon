package com.netflix.karyon.transport.tcp;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.karyon.transport.AbstractRxServerBootstrap;
import com.netflix.karyon.transport.MetricEventsListenerFactory;
import com.netflix.karyon.transport.Ports;
import io.reactivex.netty.server.ServerBuilder;
import io.reactivex.netty.server.ServerMetricsEvent;
import io.reactivex.netty.server.ServerMetricsEvent.EventType;

/**
 * @author Tomasz Bak
 */
public class TcpServerBootstrap<I, O> extends AbstractRxServerBootstrap<I, O, ServerBuilder<I, O>, ServerMetricsEvent<EventType>> {
    @Inject
    public TcpServerBootstrap(Ports ports, Injector injector,
                              MetricEventsListenerFactory<I, O, ServerMetricsEvent<EventType>> metricEventsListenerFactory,
                              ServerBuilder<I, O> serverBuilder) {
        super(ports, injector, metricEventsListenerFactory, serverBuilder);
    }
}
