package com.netflix.hellonoss.server;

import com.netflix.karyon.server.guice.KaryonGuiceContextListener;

/**
 * @author Nitesh Kant
 */
public class HelloWorldServletListener extends KaryonGuiceContextListener {

    public HelloWorldServletListener() {
        super(HelloWorldBootstrap.createNew());
    }
}
