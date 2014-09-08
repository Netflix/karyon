package com.netflix.karyon.ws.rs.writers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.google.common.collect.Lists;

public class DefaultResponseWriterFactory implements ResponseWriterFactory {
    
    private List<ResponseWriter> writers = Lists.newArrayList();
    
    public DefaultResponseWriterFactory() {
        writers.add(new JsonResponseWriter());
        writers.add(new ToStringResponseWriter());
    }
    
    @Override
    public <T> ResponseWriter<T> getResponseWriter(Class<T> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        for (ResponseWriter writer : writers) {
            if (writer.isWriteable(type, genericType, annotations, mediaType)) {
                return writer;
            }
        }
        return null;
    }

}
