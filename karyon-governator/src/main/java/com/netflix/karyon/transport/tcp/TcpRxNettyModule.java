package com.netflix.karyon.transport.tcp;

import java.lang.reflect.ParameterizedType;

import com.google.inject.util.Types;
import com.netflix.karyon.transport.AbstractRxNettyModule;
import com.netflix.karyon.transport.MetricEventsListenerFactory;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.server.ServerBuilder;
import io.reactivex.netty.server.ServerMetricsEvent;
import io.reactivex.netty.server.ServerMetricsEvent.EventType;

/**
 * @author Tomasz Bak
 */
public abstract class TcpRxNettyModule<I, O> extends AbstractRxNettyModule<I, O, ServerBuilder<I, O>, ServerMetricsEvent<EventType>> {

    private static final ParameterizedType SERVER_METRICS_EVENT_TYPE = Types.newParameterizedType(ServerMetricsEvent.class, ServerMetricsEvent.EventType.class);

    protected TcpRxNettyModule(Class<I> iType, Class<O> oType, ParameterizedType connectionHandlerType) {
        super(iType, oType,
                Types.newParameterizedType(ServerBuilder.class, iType, oType),
                Types.newParameterizedType(TcpServerBootstrap.class, iType, oType),
                Types.newParameterizedType(MetricEventsListenerFactory.class, iType, oType, SERVER_METRICS_EVENT_TYPE),
                connectionHandlerType);
    }

    @Override
    protected ServerBuilder<I, O> newServerBuilder(int port, ConnectionHandler<I, O> connectionHandler) {
        return RxNetty.newTcpServerBuilder(port, connectionHandler);
    }
}
