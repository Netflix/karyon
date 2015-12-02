package com.netflix.karyon;

public class KaryonFeature<T> {
    private final String key;
    private final T defaultValue;
    
    public static <T> KaryonFeature<T> create(String key, T defaultValue) {
        return new KaryonFeature<T>(key, defaultValue);
    }
    
    public KaryonFeature(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }
    
    public String getKey() {
        return key;
    }
    
    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) defaultValue.getClass();
    }
    
    public T getDefaultValue() {
        return defaultValue;
    }
}
