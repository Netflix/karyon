package com.netflix.karyon.ws.rs;

import io.netty.handler.codec.http.HttpMethod;

import com.sun.jersey.api.uri.UriTemplate;

/**
 * Association between a UriTemplate and a RequestHandler.
 * 
 * @author elandau
 *
 */
public class Route {
    public Route(UriTemplate template, HttpMethod verb, RequestHandler handler) {
        this.handler = handler;
        this.template = template;
        this.verb = verb;
    }
    
    final UriTemplate template;
    final RequestHandler handler;
    final HttpMethod verb;
    
    public UriTemplate getTemplate() {
        return this.template;
    }
    
    public RequestHandler getHandler() {
        return this.handler;
    }
    
    public HttpMethod getVerb() { 
        return verb;
    }
    
    @Override
    public String toString() {
        return "Route [template=" + template + ", handler=" + handler.getClass().getName()
                + ", verb=" + verb + "]";
    }
}
