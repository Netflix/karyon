package com.netflix.karyon.ws.rs.router;

/**
 * Logic for configuring a RoutingRequestHandler's builder
 * 
 * @author elandau
 *
 */
public interface RouterConfigurer {
    /**
     * Perform the configuration of a RoutingRequestHandler's builder
     * @param builder
     */
    public RoutingRequestHandler.Builder configure(RoutingRequestHandler.Builder builder);
}
