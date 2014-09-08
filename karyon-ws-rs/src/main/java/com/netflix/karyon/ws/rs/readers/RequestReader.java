package com.netflix.karyon.ws.rs.readers;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import rx.Observable;

public interface RequestReader<T> {
    boolean isReadable(
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType);

    Observable<T> readFrom(Class<T> type, 
            Type genericType,  
            Annotation annotations[], 
            MediaType mediaType,
            HttpServerRequest<ByteBuf> request
            ) throws IOException, WebApplicationException;
}
