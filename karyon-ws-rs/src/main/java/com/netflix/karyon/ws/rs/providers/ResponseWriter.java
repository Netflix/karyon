package com.netflix.karyon.ws.rs.providers;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import rx.Observable;

public interface ResponseWriter<T> {
    /**
     * Return true if this ResponseWriter can write the response for this type
     * @param type
     * @param genericType
     * @param annotations
     * @param mediaType
     * @return
     */
    boolean isWriteable(
            Class<?> type, 
            Type genericType, 
            Annotation[] annotations, 
            MediaType mediaType);
    
    long getSize(
            T t, 
            Class<?> type, 
            Type genericType, 
            Annotation[] annotations, 
            MediaType mediaType);
    
    Observable<Void> write(
            Observable<?> observable, 
            Class<?> type, 
            Type genericType,
            Annotation[] annotations, 
            MediaType mediaType,
            HttpServerResponse<ByteBuf> response) throws IOException,
            WebApplicationException;
}
