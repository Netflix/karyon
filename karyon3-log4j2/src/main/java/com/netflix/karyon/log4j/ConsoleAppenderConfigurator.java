package com.netflix.karyon.log4j;

import java.io.Serializable;
import java.nio.charset.Charset;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Log4jConfigurator;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.netflix.archaius.ConfigProxyFactory;
import com.netflix.archaius.annotations.DefaultValue;

@Singleton
public class ConsoleAppenderConfigurator implements Log4jConfigurator {
    public static interface AppenderConfig {
        @DefaultValue("%d{HH:mm:ss,SSS} [%t] %-5p %c %x %m %n")
        String getPattern();
        
        @DefaultValue("UTF-8")
        String getCharset();
        
        @DefaultValue("true")
        Boolean getAlwaysWriteExceptions();

        @DefaultValue("false")
        Boolean getNoConsoleNoAnsi();

        @Nullable
        String getHeader();
        
        @Nullable
        String getFooter();
        
        @DefaultValue("INFO")
        String getLevel();
        
        @DefaultValue("false")
        Boolean getFollow();
        
        @DefaultValue("true")
        Boolean getIgnore();

    }
    
    private final AppenderConfig _config;
    
    @Inject
    public ConsoleAppenderConfigurator(ConfigProxyFactory factory) {
        this._config = factory.newProxy(AppenderConfig.class, "log4j.appender.Console");
    }
    
    @Override
    public void doConfigure(AbstractConfiguration config) {

        Layout<? extends Serializable> layout = PatternLayout.createLayout(
                _config.getPattern(), // pattern
                config, // config
                null,   // replace
                Charset.forName(_config.getCharset()), // charset
                _config.getAlwaysWriteExceptions(),   // alwaysWriteExceptions
                _config.getNoConsoleNoAnsi(),  // noConsoleNoAnsi
                _config.getHeader(),   // header
                _config.getFooter());  // footer

        // Add the Console appender
        Appender appender;
        appender = ConsoleAppender.createAppender(layout, null, null, "Console", _config.getFollow().toString(), _config.getIgnore().toString());
        appender.start();
        config.getRootLogger().addAppender(appender, Level.valueOf(_config.getLevel()), null);
    }

}