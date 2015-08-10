package com.netflix.karyon.log4j;

import org.apache.logging.log4j.core.ArchaiusLog4J2ConfigurationFactory;
import org.apache.logging.log4j.core.Log4jConfigurator;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Add this to the list of Guice modules to enable reconfiguration of log4j2
 * from properties defined in application.properties.  Also, reconfigure on 
 * a set interval to pick up changes to log levels from dynamic properties.
 * Finally add the console appender using a multibinder.
 * 
 * @author elandau
 *
 */
public class ArchaiusLog4J2ConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        this.requestStaticInjection(ArchaiusLog4J2ConfigurationFactory.class);

        Multibinder<Log4jConfigurator> appenderBinder = Multibinder.newSetBinder(binder(), Log4jConfigurator.class);
        appenderBinder.addBinding().to(ConsoleAppenderConfigurator.class);

    }
}
