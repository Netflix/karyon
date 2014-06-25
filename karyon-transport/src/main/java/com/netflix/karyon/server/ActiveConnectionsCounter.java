package com.netflix.karyon.server;

import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.monitor.NumberGauge;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Nitesh Kant
 */
@ChannelHandler.Sharable
public class ActiveConnectionsCounter extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveConnectionsCounter.class);

    private final NumberGauge activeConnections;

    public ActiveConnectionsCounter(String name) {
        String counterName = name + "-active_connections";
        activeConnections = new NumberGauge(MonitorConfig.builder(counterName).build(), new AtomicLong());
        try {
            Monitors.registerObject(this);
        } catch (Exception e) {
            LOGGER.error("Failed to register servo monitor object with name: " + counterName, e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ((AtomicLong) activeConnections.getValue()).incrementAndGet();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ((AtomicLong) activeConnections.getValue()).decrementAndGet();
    }

}
