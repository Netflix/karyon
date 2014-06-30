package com.netflix.karyon.transport.http;

import com.netflix.karyon.transport.interceptor.InterceptorKey;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;

/**
 * @author Nitesh Kant
 */
public interface HttpInterceptorKey<I> extends InterceptorKey<HttpServerRequest<I>, HttpKeyEvaluationContext>{
}
