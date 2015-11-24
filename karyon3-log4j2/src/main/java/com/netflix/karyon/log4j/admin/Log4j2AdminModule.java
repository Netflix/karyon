package com.netflix.karyon.log4j.admin;

import com.google.inject.AbstractModule;

public class Log4j2AdminModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Log4JResource.class);
    }
}
