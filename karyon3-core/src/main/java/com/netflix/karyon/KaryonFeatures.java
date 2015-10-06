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
    SHUTDOWN_ON_ERROR(true, "karyon.features.shutdownOnError"),
    
    /**
     * When enabled will auto install Karyon's default settings including classpath
     * scanning of 'com.netflix.karyon' for conditional modules
     */
    USE_DEFAULT_KARYON_MODULE(true, "karyon.features.defaultModule"), 
    
    /**
     * When enabled will auto install the Karyon-Archaius integration unless already
     * installed.
     */
    USE_ARCHAIUS(true, "karyon.features.archaius"),
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
