package com.netflix.karyon.ws.rs.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

public class MessageBodyWriterProxy<T> implements MessageBodyWriter<T> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return false;
    }

    @Override
    public long getSize(T t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException,
            WebApplicationException {
        // TODO Auto-generated method stub
        
    }

}
