package com.netflix.karyon;

/**
 * Core karyon features.  Features are configured/enabled on {@link Karyon}
 * 
 * @author elandau
 */
public final class KaryonFeatures  {
    /**
     * When disable the Karyon process will continue running even if there is a catastrophic 
     * startup failure.  This allows the admin page to stay up so that the process may be 
     * debugged more easily. 
     */
    public static final KaryonFeature<Boolean> SHUTDOWN_ON_ERROR = KaryonFeature.create("karyon.features.shutdownOnError", true);
    
    /**
     * Auto discover AutoBinders using the ServiceLoader
     */
    public static final KaryonFeature<Boolean> DISCOVER_AUTO_BINDERS = KaryonFeature.create("karyon.features.discoverAutoBinders", true);
    
    @Deprecated
    public static final KaryonFeature<Boolean> USE_ARCHAIUS = KaryonFeature.create("karyon.features.archaius", false);
}
