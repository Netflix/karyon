package com.netflix.karyon.ws.rs;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Encapsulate the request context with route scoping so that each route's context
 * is relative to it's parent
 * 
 * @author elandau
 *
 */
public class RequestContext {
    /**
     * Variables extracted from the Path for the Route context
     */
    private Map<String, String> vars;
    
    /**
     * Response object.  
     */
    private HttpServerResponse<ByteBuf> response;
    
    /**
     * Full request object
     */
    private HttpServerRequest<ByteBuf> request;
    private String fragment;
    
    public RequestContext(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
        this(request, response, Maps.<String, String>newHashMap(), request.getPath());
    }
    
    public RequestContext(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response, Map<String, String> vars, String fragment) {
        this.request = request;
        this.response = response;
        this.fragment = fragment;
        this.vars = vars;
    }

    public HttpServerRequest<ByteBuf> getRequest() {
        return request;
    }
    
    public HttpServerResponse<ByteBuf> getResponse() {
        return response;
    }
    
    public Map<String, String> getVars() {
        return vars;
    }

    public String getFragment() {
        return fragment;
    }

    /**
     * Construct a child of this RequestContext with the remaining fragment and extracted
     * path variables
     * @param vars
     * @param fragment
     * @return
     */
    public RequestContext getChild(Map<String, String> vars, String fragment) {
        return new RequestContext(request, response, vars, fragment);
    }
}
