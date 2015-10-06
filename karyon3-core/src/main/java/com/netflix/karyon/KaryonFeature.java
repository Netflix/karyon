package com.netflix.karyon;

/**
 * Base interface for all {@link Karyon} features to be implemented by an 
 * enum, such as {@link KaryonFeatures}.  Each feature has an implicit
 * default value. 
 * 
 * @see Karyon
 * @author elandau
 */
public interface KaryonFeature {
    boolean isEnabledByDefault();
    String getKey();
}
