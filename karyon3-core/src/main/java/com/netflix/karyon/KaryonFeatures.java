package com.netflix.karyon;

/**
 * Core karyon features.  Features are configured/enabled on {@link KaryonConfiguration}
 * 
 * @author elandau
 */
public final class KaryonFeatures  {
    /**
     * When disable the Karyon process will continue running even if there is a catastrophic 
     * startup failure.  This allows the admin page to stay up so that the process may be 
     * debugged more easily. 
     */
    public final static KaryonFeature<Boolean> SHUTDOWN_ON_ERROR = KaryonFeature.create("karyon.features.shutdownOnError", Boolean.class, true);
    
    @Deprecated
    public final static KaryonFeature<Boolean> USE_ARCHAIUS = KaryonFeature.create("karyon.features.archaius", Boolean.class, false);
}
