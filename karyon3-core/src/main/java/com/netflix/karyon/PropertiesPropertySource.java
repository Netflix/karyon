package com.netflix.karyon;

import java.util.Properties;

import javax.inject.Singleton;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.netflix.karyon.api.PropertySource;

public class PropertiesPropertySource extends AbstractPropertySource {
    private Properties props;

    public PropertiesPropertySource(Properties props) {
        this.props = props;
    }

    public static PropertiesPropertySource from(Properties props) {
        return new PropertiesPropertySource(props);
    }
    
    public static Module toModule(final Properties props) {
        return new AbstractModule() {
            @Provides
            @Singleton
            public PropertySource getConfig() {
                return new PropertiesPropertySource(props);
            }

            @Override
            protected void configure() {
            }
        };
    }
    
    @Override
    public String get(String key) {
        return props.getProperty(key);
    }

    @Override
    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
