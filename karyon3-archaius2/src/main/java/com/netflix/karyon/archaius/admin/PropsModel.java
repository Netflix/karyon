package com.netflix.karyon.archaius.admin;

import java.util.Map;

final class PropsModel {
    private final Map<String, String> props;
    
    public PropsModel(Map<String, String> props) {
        this.props = props;
    }
    
    public Map<String, String> getProps() {
        return props;
    }
}
