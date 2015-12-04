package com.netflix.karyon.api;

import com.netflix.karyon.KaryonFeature;

/**
 * Container of karyon features.
 */
public interface KaryonFeatureSet {
    /**
     * @return Get the value of the feature or the default if none is set
     */
    <T> T get(KaryonFeature<T> feature);
}
