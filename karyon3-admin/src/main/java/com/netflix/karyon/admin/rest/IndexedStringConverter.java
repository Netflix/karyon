package com.netflix.karyon.admin.rest;

import java.util.List;

public class IndexedStringConverter {
    private final int index;
    private final StringConverter converter;
    
    public IndexedStringConverter(int index, StringConverter converter) {
        this.index = index;
        this.converter = converter;
    }
    
    public Object convert(List<String> value) throws Exception {
        return converter.convert(value.get(index));
    }
}
