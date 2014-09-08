package com.netflix.karyon.ws.rs.readers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

public interface RequestReaderFactory {
    <T> RequestReader<T> getRequestReader(
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType);
}
