package com.netflix.karyon.transport.http;

/**
 * @author Nitesh Kant
 */
public class Ports {

    private int listenPort;
    private int shutdownPort;

    public Ports(int listenPort, int shutdownPort) {
        this.listenPort = listenPort;
        this.shutdownPort = shutdownPort;
    }

    public int getListenPort() {
        return listenPort;
    }

    public int getShutdownPort() {
        return shutdownPort;
    }
}
