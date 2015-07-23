package com.netflix.karyon.admin.rest;

import java.util.List;

public class IndexedStringResolver {
    private final int index;
    private final StringResolver converter;
    
    public IndexedStringResolver(int index, StringResolver converter) {
        this.index = index;
        this.converter = converter;
    }
    
    public Object convert(List<String> value) throws Exception {
        return converter.convert(value.get(index));
    }
}
