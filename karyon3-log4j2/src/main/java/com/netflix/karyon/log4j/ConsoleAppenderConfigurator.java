package com.netflix.karyon.log4j;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Log4jConfigurator;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.google.common.base.Charsets;
import com.netflix.archaius.Config;

@Singleton
public class ConsoleAppenderConfigurator implements Log4jConfigurator {
    private static final String PROP_LOG4J_PATTERN = "log4j.appender.Console.pattern";
    
    private static final String DEFAULT_PATTERN = "%d{HH:mm:ss,SSS} [%t] %-5p %c %x %m %n";

    private final Config _config;
    
    @Inject
    public ConsoleAppenderConfigurator(Config config) {
        this._config = config;
    }
    
    @Override
    public void doConfigure(AbstractConfiguration config) {

        Layout<? extends Serializable> layout = PatternLayout.createLayout(_config.getString(PROP_LOG4J_PATTERN, DEFAULT_PATTERN), // pattern
                config, // config
                null,   // replace
                Charsets.UTF_8, // charset
                true,   // alwaysWriteExceptions
                false,  // noConsoleNoAnsi
                null,   // header
                null);  // footer

        // Add the Console appender
        Appender appender;
        appender = ConsoleAppender.createAppender(layout, null, null, "Console", null, null);
        appender.start();
        config.getRootLogger().addAppender(appender, Level.INFO, null);
    }

}