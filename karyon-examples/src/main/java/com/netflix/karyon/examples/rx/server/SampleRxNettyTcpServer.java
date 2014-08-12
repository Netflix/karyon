package com.netflix.karyon.examples.rx.server;

import java.nio.charset.Charset;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.governator.annotations.Modules;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.archaius.ArchaiusBootstrap;
import com.netflix.karyon.examples.hellonoss.server.health.HealthCheck;
import com.netflix.karyon.examples.rx.server.SampleRxNettyTcpServer.SampleRxNettyTcpModule;
import com.netflix.karyon.transport.MetricEventsListenerFactory;
import com.netflix.karyon.transport.Ports;
import com.netflix.karyon.transport.tcp.TcpRxNettyModule;
import com.netflix.karyon.transport.tcp.TcpServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.server.ServerBuilder;
import io.reactivex.netty.server.ServerMetricsEvent;
import io.reactivex.netty.server.ServerMetricsEvent.EventType;
import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tomasz Bak
 */
@ArchaiusBootstrap
@KaryonBootstrap(name = "sample-rxnetty-tcp-noss", healthcheck = HealthCheck.class)
@Modules(include = {
        SampleRxNettyTcpModule.class
//        KaryonWebAdminModule.class,
        // Uncomment the following line to enable eureka. Make sure eureka-client.properties is configured to point to your eureka server.
        //, KaryonEurekaModule.class
})
public class SampleRxNettyTcpServer {

    public static class SampleServerBootstrap extends TcpServerBootstrap<ByteBuf, ByteBuf> {
        @Inject
        public SampleServerBootstrap(Ports ports, Injector injector, MetricEventsListenerFactory<ByteBuf, ByteBuf, ServerMetricsEvent<EventType>> metricEventsListenerFactory, ServerBuilder<ByteBuf, ByteBuf> serverBuilder) {
            super(ports, injector, metricEventsListenerFactory, serverBuilder);
        }
    }

    public static class SampleRxNettyTcpModule extends TcpRxNettyModule<ByteBuf, ByteBuf> {

        public SampleRxNettyTcpModule() {
            super(ByteBuf.class, ByteBuf.class);
        }

        @Override
        public int serverPort() {
            return 9988;
        }

        @Override
        public int shutdownPort() {
            return 9999;
        }

        @Override
        public ConnectionHandler<ByteBuf, ByteBuf> connectionHandler() {
            return new ConnectionHandler<ByteBuf, ByteBuf>() {
                @Override
                public Observable<Void> handle(final ObservableConnection<ByteBuf, ByteBuf> connection) {
                    System.out.println("Got new connection");
                    return connection.getInput().flatMap(new Func1<ByteBuf, Observable<Void>>() {
                        @Override
                        public Observable<Void> call(ByteBuf byteBuf) {
                            System.out.println("Received: " + byteBuf.toString(Charset.defaultCharset()));
                            ByteBuf output = connection.getAllocator().buffer();
                            output.writeBytes("Want some more".getBytes());
                            return connection.writeAndFlush(output);
                        }
                    });
                }
            };
        }

        @Override
        public MetricEventsListenerFactory<ByteBuf, ByteBuf, ServerMetricsEvent<EventType>> metricsEventsListenerFactory() {
            return new MetricEventsListenerFactory.TcpMetricEventsListenerFactory<ByteBuf, ByteBuf>();
        }

    }
}
