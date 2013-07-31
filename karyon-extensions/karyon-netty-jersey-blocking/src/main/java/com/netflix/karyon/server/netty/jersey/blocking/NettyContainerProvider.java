package com.netflix.karyon.server.netty.jersey.blocking;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerProvider;
import com.sun.jersey.spi.container.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nitesh Kant
 */
public class NettyContainerProvider implements ContainerProvider<NettyContainer> {

    private static Logger logger = LoggerFactory.getLogger(NettyContainerProvider.class);

    @Override
    public NettyContainer createContainer(Class<NettyContainer> type, ResourceConfig resourceConfig,
                                          WebApplication application) throws ContainerException {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(application);
        if (!type.equals(NettyContainer.class)) {
            logger.error(
                    "Netty container provider can only create container of type {}. Invoked to create container of type {}",
                    NettyContainer.class.getName(), type.getName());
        }
        return new NettyContainer(application);
    }
}
