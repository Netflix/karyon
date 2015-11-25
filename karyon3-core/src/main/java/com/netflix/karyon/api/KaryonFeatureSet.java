package com.netflix.karyon.api;

import com.netflix.karyon.KaryonFeature;

/**
 * Container of karyon features.
 */
public interface KaryonFeatureSet {
    /**
     * Get the value of the feature or the default if none is set
     * 
     * @param feature
     * @return
     */
    <T> T get(KaryonFeature<T> feature);
}
