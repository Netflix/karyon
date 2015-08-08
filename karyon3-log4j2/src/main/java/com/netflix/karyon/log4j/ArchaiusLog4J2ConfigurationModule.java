package com.netflix.karyon.log4j;

import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Log4jConfiguration;
import org.apache.logging.log4j.core.ArchaiusLog4J2ConfigurationFactory;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.google.common.base.Charsets;
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
        
        Multibinder<Log4jConfiguration> appenderBinder = Multibinder.newSetBinder(binder(),
				Log4jConfiguration.class);
		appenderBinder.addBinding().to(ConsoleAppenderConfiguration.class);

    }
    
    public static class ConsoleAppenderConfiguration implements Log4jConfiguration
    {

		@Override
		public void doConfigure(XmlConfiguration config) {
		
            Layout<? extends Serializable> layout = PatternLayout.createLayout(
                    "%d %-5p %c{1}:%L %x %m [%t]%n",  // pattern
                    config,           // config
                    null,           // replace
                    Charsets.UTF_8, // charset
                    true,           // alwaysWriteExceptions
                    false,          // noConsoleNoAnsi
                    null,           // header
                    null);          // footer

            // Add the Console appender
            Appender appender;
            appender = ConsoleAppender.createAppender(layout, null, null, "Console",  null,  null);
            appender.start();
            config.getRootLogger().addAppender(appender, Level.INFO, null);
		}
    	
    }
}
