package com.netflix.karyon;

import com.netflix.governator.DefaultGovernatorConfiguration;
import com.netflix.governator.GovernatorConfiguration;

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
    
    public static Builder builder() {
        return new Builder() {
            @Override
            protected Builder This() {
                return this;
            }
        };
    }
    
    public DefaultKaryonConfiguration() {
    }
    
    protected DefaultKaryonConfiguration(Builder<?> builder) {
        super(builder);
    }
}
