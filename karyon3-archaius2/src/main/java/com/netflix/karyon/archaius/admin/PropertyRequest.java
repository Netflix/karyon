package com.netflix.karyon.archaius.admin;

public class PropertyRequest {
    String layerName;
    String key;
    String regex;
    
    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }
    public String getLayerName() {
        return this.layerName;
    }
    public void setRegex(String regex) {
        this.regex = regex;
    }
    public String getRegex() {
        return this.regex;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
}
