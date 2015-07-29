package org.apache.logging.log4j.core;

import java.io.Serializable;
import java.util.Iterator;

import javax.inject.Inject;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.google.common.base.Charsets;
import com.netflix.archaius.Config;

/**
 * Archaius based ConfigurationFactory for log4j2 which offers two key functionalities
 * 1.  Load all configuration from archaius (i.e application.properties, persisted properties, )
 * 2.  Update configuration at runtime by calling 
 *       ((LoggerContext) LogManager.getContext(false)).reconfigure();
 *
 * To enable in a Karyon application simply add ArchaiusLog4jConfigurationModule to the list
 * of modules.  
 * 
 * IMPORTANT:
 * While log4j2 does have a robust plugin architecture it's very tricky to configure an 
 * ConfigurationFactory plugin since the ConfigurationFactory must be initialized before the 
 * plugin manager.  To make this possible we make the ArchaiusLog4JConfigurationFactory look 
 * like one of the log4j2 internal factories by putting it in the org.apache.logging.log4j.core 
 * package and referencing it from META-INF/org/apach/logging/log4j/core/config/plugins/Log4j2Plugins.dat. 
 * 
 * @author elandau
 *
 */
@Plugin(name = "ArchaiusLog4j2ConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(10)
public class ArchaiusLog4J2ConfigurationFactory extends ConfigurationFactory {
    
    public static final String[] SUFFIXES = new String[] {".xml", "*"};
    
    private static Config config;
    
    @Inject
    public static void initialize(Config _config) {
        config = _config;
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.reconfigure();
    }
    
    @Override
    protected String[] getSupportedTypes() {
        return SUFFIXES;
    }

    @Override
    public Configuration getConfiguration(ConfigurationSource source) {
        if (config != null) {
            System.out.println("Creating archaius based Configuration");
            return new ArchaiusLog4j2Configuration(source);
        }
        else {
            System.out.println("Creating default XmlConfiguration");
            return new XmlConfiguration(source);
        }
    }
    
    public static class ArchaiusLog4j2Configuration extends XmlConfiguration {
        private static final long serialVersionUID = 1L;
        
        public ArchaiusLog4j2Configuration(ConfigurationSource configSource) {
            super(configSource);
        }

        @Override
        protected void doConfigure() {
            super.doConfigure();
            
            Layout<? extends Serializable> layout = PatternLayout.createLayout(
                    "%d %-5p %c{1}:%L %x %m [%t]%n",  // pattern
                    this,           // config
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
            getRootLogger().addAppender(appender, Level.INFO, null);
            
            // TOD: Get materialized view
            
            // Set up all the log level overrides
            Config loggerProps = config.getPrefixedView("log4j.logger");
            Iterator<String> iter = loggerProps.getKeys();
            while (iter.hasNext()) {
                String name = iter.next();
                String value = loggerProps.getString(name); // TODO: Full log4j.logger value parsing
                System.out.println("Setting logger " + name + " => " + value);
                this.addLogger(name, new LoggerConfig(name, Level.getLevel(value), true));
            }
        }
    }
}