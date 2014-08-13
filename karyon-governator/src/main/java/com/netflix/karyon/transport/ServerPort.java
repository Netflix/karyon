package com.netflix.karyon.transport;

/**
 * @author Tomasz Bak
 */
public class ServerPort {

    private final int port;

    public ServerPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
