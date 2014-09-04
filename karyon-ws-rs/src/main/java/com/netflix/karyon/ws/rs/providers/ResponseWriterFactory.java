package com.netflix.karyon.ws.rs.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

public interface ResponseWriterFactory {
    <T> ResponseWriter<T> getResponseWriter(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType);
}
