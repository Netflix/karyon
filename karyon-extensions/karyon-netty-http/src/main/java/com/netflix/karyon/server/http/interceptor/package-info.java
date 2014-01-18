/**
 * Interceptors are based on the <a href="http://en.wikipedia.org/wiki/Interceptor_pattern">Interceptor pattern</a> and
 * are used to implement cross-cutting concerns across all or a set of HTTP requests. <br/>
 * One can configure which interceptors are to be applied for which requests (based on a criterion) using
 * {@link InterceptorPipelineBuilder} which creates an instance of {@link PipelineDefinition} which in turn is used by
 * {@link PipelineFactory} at runtime to deduce which interceptors are to be applied for a specific request. <br/>
 *
 * <h2>Interception direction</h2>
 *
 * Interception is done before and after executing the {@link HttpRequestRouter}. <br/>
 * All {@link InboundInterceptor} are invoked before invoking the router. <br/>
 * All {@link OutboundInterceptor} are invoked after the router has indicated response sending by calling
 * {@link HttpResponseWriter#sendResponse()}. <br/>
 *
 * An interceptor pipeline (inbound or outbound) follows the
 * <a href="http://en.wikipedia.org/wiki/Chain-of-responsibility_pattern">Chain of Responsibility</a> pattern and hence
 * every interceptor in the chain has the responsibility of invoking the next interceptor in the pipeline using
 * {@link InterceptorExecutionContext#executeNextInterceptor(FullHttpRequest, StatefulHttpResponseWriter)}. Failure to do so, will break
 * the chain and hence the processing of the request or sending or response. <br/>
 * Invocation of {@link InterceptorExecutionContext#executeNextInterceptor(FullHttpRequest, StatefulHttpResponseWriter)} by the
 * last interceptor in the inbound chain will invoke the router. <br/>
 * Invocation of {@link InterceptorExecutionContext#executeNextInterceptor(FullHttpRequest, StatefulHttpResponseWriter)} by the
 * last interceptor in the outbound chain will send the response to the client. <br/>
 */
package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.StatefulHttpResponseWriter;