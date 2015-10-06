package com.netflix.karyon;

/**
 * Base interface for all governator features to be implemented by an 
 * enum, such as {@link KaryonFeatures}.  Each feature has an implicit
 * default value if not specified.  Features are set on KaryonConfiguration.
 * 
 * @author elandau
 */
public interface KaryonFeature {
    boolean isEnabledByDefault();
    String getKey();
}
