package com.netflix.karyon.api;

/**
 * Base interface for all Karyon features to be implemented by an 
 * enum, such as {@link KaryonFeatures}.  Each feature has an implicit
 * default value. 
 * 
 * @author elandau
 */
public interface KaryonFeature {
    boolean isEnabledByDefault();
    String getKey();
}
