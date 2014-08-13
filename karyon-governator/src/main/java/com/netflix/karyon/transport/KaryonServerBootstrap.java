package com.netflix.karyon.transport;

/**
 * @author Tomasz Bak
 */
public interface KaryonServerBootstrap {

    /**
     * Start the server and exit (this method does not block).
     */
    void startServer() throws Exception;

    void shutdown() throws InterruptedException;

    /**
     * If server is running, wait until it is shutdown.
     */
    void waitTillShutdown() throws InterruptedException;
}
