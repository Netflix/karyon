package com.netflix.karyon;

import com.netflix.governator.DefaultGovernatorConfiguration;
import com.netflix.governator.GovernatorConfiguration;
import com.netflix.governator.auto.ModuleListProviders;

/**
 * Default implementation of KaryonConfiguration.
 * 
 * @author elandau
 *
 */
public class DefaultKaryonConfiguration extends DefaultGovernatorConfiguration {
    /**
     * Polymorphic builder.
     * 
     * @author elandau
     *
     * @param <T>
     */
    public static abstract class Builder<T extends Builder<T>> extends DefaultGovernatorConfiguration.Builder<T> {
        public GovernatorConfiguration build() {
            return new DefaultKaryonConfiguration(this);
        }
    }
    
    private static class BuilderWrapper extends Builder<BuilderWrapper> {
        @Override
        protected BuilderWrapper This() {
            return this;
        }
    }

    public static Builder<?> builder() {
        return new BuilderWrapper();
    }
    
    public DefaultKaryonConfiguration() {
        super(builder()
            .addModuleListProvider(ModuleListProviders.forPackagesConditional("com.netflix.karyon"))
            );
    }
    
    protected DefaultKaryonConfiguration(Builder<?> builder) {
        super(builder
            .addModuleListProvider(ModuleListProviders.forPackagesConditional("com.netflix.karyon"))
            );
    }
}
