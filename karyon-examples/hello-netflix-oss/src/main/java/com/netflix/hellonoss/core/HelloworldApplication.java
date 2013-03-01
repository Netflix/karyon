package com.netflix.hellonoss.core;

import com.netflix.karyon.spi.Application;

import javax.annotation.PostConstruct;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
@Application
public class HelloworldApplication {

    @PostConstruct
    public void initialize() {
        //TODO: Initialization if any.
    }
}
