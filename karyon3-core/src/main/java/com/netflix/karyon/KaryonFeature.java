package com.netflix.karyon;

public class KaryonFeature<T> {
    private final String key;
    private final Class<T> type;
    private final T defaultValue;
    
    public static <T> KaryonFeature<T> create(String key, Class<T> type, T defaultValue) {
        return new KaryonFeature<T>(key, type, defaultValue);
    }
    
    public KaryonFeature(String key, Class<T> type, T defaultValue) {
        super();
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
    }
    
    public String getKey() {
        return key;
    }
    
    public Class<T> getType() {
        return type;
    }
    
    public T getDefaultValue() {
        return defaultValue;
    }
}
