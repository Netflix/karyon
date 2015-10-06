package com.netflix.karyon;

/**
 * Core karyon features.  Features are configured/enabled on {@link KaryonConfiguration}
 * 
 * @author elandau
 */
public enum KaryonFeatures implements KaryonFeature {
    /**
     * When disable the Karyon process will continue running even if there is a catastrophic 
     * startup failure.  This allows the admin page to stay up so that the process may be 
     * debugged more easily. 
     */
    SHUTDOWN_ON_ERROR(true, "karyon.feature.shutdownOnError"),
    
    USE_DEFAULT_PACKAGES(true, "karyon.feature.defaultPackages"),
    ;

    private final boolean enabled;
    private final String key;
    
    KaryonFeatures(boolean enabled, String key) {
        this.enabled = enabled;
        this.key = key;
    }
    
    @Override
    public boolean isEnabledByDefault() {
        return enabled;
    }

    @Override
    public String getKey() {
        return key;
    }
}
