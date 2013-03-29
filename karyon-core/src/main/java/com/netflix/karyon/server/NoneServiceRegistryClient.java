package com.netflix.karyon.server;

import com.netflix.karyon.spi.ServiceRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A no-op service regisrty client that gets bound to {@link ServiceRegistryClient} if there isn't a service registry
 * required. This is just to avoid guarded calls to the service registry client.
 *
 * @author Nitesh Kant
 */
public class NoneServiceRegistryClient implements ServiceRegistryClient {

    private static final Logger logger = LoggerFactory.getLogger(NoneServiceRegistryClient.class);

    @Override
    public void updateStatus(ServiceStatus newStatus) {
        logger.debug("NoneServiceRegistry client invoked with status: " + newStatus +
                     ", this call will be ignored as you have specified not to use a service registry.");
    }
}
