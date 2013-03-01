package com.netflix.hellonoss.core;

import com.netflix.karyon.spi.Component;

import javax.annotation.PostConstruct;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
@Component
public class HelloworldComponent {

    @PostConstruct
    public void initialize() {
        // TODO: Initialization logic, eg: connection to DB etc.
    }
}
