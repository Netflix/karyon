package com.netflix.karyon.ws.rs.providers;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import rx.Observable;
import rx.functions.Func1;

public class JsonResponseWriter implements ResponseWriter<Object> {
    private ObjectMapper mapper = new ObjectMapper();
    
    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE))
            return true;
        return false;
    }

    @Override
    public long getSize(Object t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public Observable<Void> write(Observable<?> source, Class<?> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            final HttpServerResponse<ByteBuf> response) throws IOException,
            WebApplicationException {
        return source.flatMap(new Func1<Object, Observable<Void>>() {
            @Override
            public Observable<Void> call(Object obj) {
                try {
                    return response.writeStringAndFlush(mapper.writeValueAsString(obj));
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        });
    }



}
