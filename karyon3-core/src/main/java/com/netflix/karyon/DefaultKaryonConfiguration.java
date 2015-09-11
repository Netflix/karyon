package com.netflix.karyon;

import com.netflix.governator.DefaultGovernatorConfiguration;
import com.netflix.governator.auto.ModuleListProviders;

/**
 * Default implementation of KaryonConfiguration.
 * 
 * @author elandau
 *
 */
public class DefaultKaryonConfiguration extends DefaultGovernatorConfiguration implements KaryonConfiguration {
    private static final String KARYON_PROFILES = "karyon.profiles";
    
    /**
     * Polymorphic builder.
     * 
     * @author elandau
     *
     * @param <T>
     */
    public static abstract class Builder<T extends Builder<T>> extends DefaultGovernatorConfiguration.Builder<T> {
        protected Builder() {
            addModule(new CoreModule());
            addModuleListProvider(ModuleListProviders.forPackagesConditional("com.netflix.karyon"));
            
            String karyonProfiles = System.getProperty(KARYON_PROFILES);
            if (karyonProfiles != null) {
                addProfiles(karyonProfiles);
            }
        }
        
        public DefaultKaryonConfiguration build() throws Exception {
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
    
    public static DefaultKaryonConfiguration createDefault() throws Exception {
        return builder().build();
    }

    public DefaultKaryonConfiguration(Builder<?> builder) {
        super(builder);
    }
}
