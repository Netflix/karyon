package com.netflix.karyon.examples.hellonoss.server.rxnetty;

import com.netflix.karyon.Karyon;
import com.netflix.karyon.ShutdownModule;
import com.netflix.karyon.archaius.ArchaiusSuite;

/**
 * @author Nitesh Kant
 */
public class MyRunner {

    public static void main(String[] args) {
        Karyon.forRequestHandler(8888, new RxNettyHandler(),
                                 new ArchaiusSuite("hello-netflix-oss"),
                                 ShutdownModule.asSuite()).startAndWaitTillShutdown();
    }
}
