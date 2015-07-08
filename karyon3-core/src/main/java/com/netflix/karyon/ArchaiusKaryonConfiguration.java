package com.netflix.karyon;

import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.netflix.archaius.guice.ArchaiusModule;

public class ArchaiusKaryonConfiguration extends DefaultKaryonConfiguration {
    private static final String DEFAULT_CONFIG_NAME = "application";
    
    public static class Builder<T extends Builder> extends DefaultKaryonConfiguration.Builder{
        private String                      configName = "application";

        /**
         * Configuration name to use for property loading.  Default configuration
         * name is 'application'.  This value is injectable as
         *  
         *      @Named("karyon.configName") String configName
         * 
         * @param value
         * @return
         */
        public T withConfigName(String value) {
            this.configName = value;
            return This();
        }
        
        public T This() {
            return (T) this;
        }

        public ArchaiusKaryonConfiguration build() {
            return new ArchaiusKaryonConfiguration(this);
        };
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    private final String configName;
    
    public ArchaiusKaryonConfiguration() {
        super();
        
        configName = DEFAULT_CONFIG_NAME;
    }
    
    private ArchaiusKaryonConfiguration(Builder builder) {
        super(builder);
        this.configName = builder.configName;
    }
    
    public List<Module> getBootstrapModules() {
        List<Module> modules = super.getBootstrapModules();
        modules.add(new ArchaiusModule()); 
        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                this.bindConstant().annotatedWith(Names.named("karyon.configName")).to(getConfigurationName());
            }
        });
        
        return modules;
    }
    
    public String getConfigurationName() {
        return configName;
    }
}
