package com.netflix.hellonoss.server;

import com.netflix.karyon.KaryonServer;

/**
 * @author Nitesh Kant
 */
public class Launcher {

    public static void main(String[] args) throws ClassNotFoundException {
        KaryonServer.main(new String[] {"com.netflix.hellonoss.server.Server"});
    }
}
