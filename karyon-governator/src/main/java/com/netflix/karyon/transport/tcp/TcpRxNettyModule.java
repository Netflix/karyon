package com.netflix.karyon.transport.tcp;

import java.lang.reflect.ParameterizedType;

import com.netflix.karyon.transport.AbstractRxNettyModule;
import com.netflix.karyon.transport.MetricEventsListenerFactory;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.server.ServerBuilder;
import io.reactivex.netty.server.ServerMetricsEvent;
import io.reactivex.netty.server.ServerMetricsEvent.EventType;
import org.hibernate.validator.jtype.Types;

/**
 * @author Tomasz Bak
 */
public abstract class TcpRxNettyModule<I, O> extends AbstractRxNettyModule<I, O, ServerBuilder<I, O>, ServerMetricsEvent<EventType>> {

    private static final ParameterizedType SERVER_METRICS_EVENT_TYPE = Types.parameterizedType(ServerMetricsEvent.class, ServerMetricsEvent.EventType.class);

    protected TcpRxNettyModule(Class<I> iType, Class<O> oType) {
        super(Types.parameterizedType(ServerBuilder.class, iType, oType),
                Types.parameterizedType(TcpServerBootstrap.class, iType, oType),
                Types.parameterizedType(MetricEventsListenerFactory.class, iType, oType, SERVER_METRICS_EVENT_TYPE));
    }

    @Override
    protected ServerBuilder<I, O> newServerBuilder(int port, ConnectionHandler<I, O> connectionHandler) {
        return RxNetty.newTcpServerBuilder(port, connectionHandler);
    }
}
