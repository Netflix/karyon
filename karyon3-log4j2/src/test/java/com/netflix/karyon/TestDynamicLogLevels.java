package com.netflix.karyon;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import junit.framework.Assert;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Log4jConfigurator;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.config.SettableConfig;
import com.netflix.archaius.exceptions.ConfigException;
import com.netflix.archaius.inject.RuntimeLayer;
import com.netflix.karyon.archaius.ArchaiusKaryonModule;
import com.netflix.karyon.log4j.ArchaiusLog4J2ConfigurationModule;

public class TestDynamicLogLevels {
    @Singleton
    public static class TestAppender extends AbstractAppender {
        private static final long serialVersionUID = 1L;
        private volatile String lastEvent;
        
        protected TestAppender() {
            super("test", null, null);
            System.out.println("***** TestAppender()");
        }

        @Override
        public void append(LogEvent event) {
            lastEvent = event.getMessage().getFormattedMessage();
        }
        
        public String getLastEvent() {
            return lastEvent;
        }
    }
    
    @Singleton
    public static class TestConfigurator implements Log4jConfigurator {
        private TestAppender appender;
        
        @Inject
        public TestConfigurator(TestAppender appender) {
            this.appender = appender;
        }
        
        @Override
        public void doConfigure(AbstractConfiguration config) {
            appender.start();
            config.getRootLogger().addAppender(appender, Level.INFO, null);
            config.addAppender(this.appender);
        }
    }
    
    @Test
    public void testDynamicConfigurationChange() throws ConfigException, InterruptedException {
        Injector injector = Karyon.newBuilder()
            .addModules(
                new ArchaiusKaryonModule()
                    .withRuntimeOverrides(MapConfig.builder()
                        .put("log4j.logger.com.netflix.karyon", "WARN")
                        .build()),
                new ArchaiusLog4J2ConfigurationModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        Multibinder<Log4jConfigurator> appenderBinder = Multibinder.newSetBinder(binder(), Log4jConfigurator.class);
                        appenderBinder.addBinding().to(TestConfigurator.class);
                    }
                }
            )
            .start();
        
        Logger log = LoggerFactory.getLogger(TestDynamicLogLevels.class);
        
        SettableConfig config = injector.getInstance(Key.get(SettableConfig.class, RuntimeLayer.class));
        TestAppender appender = injector.getInstance(TestAppender.class);
        
        log.warn("test_warn");
        TimeUnit.MILLISECONDS.sleep(50);
        Assert.assertEquals("test_warn", appender.getLastEvent());
        
        log.info("test_info1");
        TimeUnit.MILLISECONDS.sleep(50);
        Assert.assertEquals("test_warn", appender.getLastEvent());
        
        config.setProperty("log4j.logger.com.netflix.karyon", "INFO");
        TimeUnit.MILLISECONDS.sleep(50);
        log.info("test_info2");
        TimeUnit.MILLISECONDS.sleep(50);
        Assert.assertEquals("test_info2", appender.getLastEvent());

        
    }
}
