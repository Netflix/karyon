/**
 * Interceptors are based on the <a href="http://en.wikipedia.org/wiki/Interceptor_pattern">Interceptor pattern</a> and
 * are used to implement cross-cutting concerns across all or a set of HTTP requests. <br/>
 * One can configure which interceptors are to be applied for which requests (based on a criterion) using
 * {@link InterceptorPipelineBuilder} which creates an instance of {@link PipelineDefinition} which in turn is used by
 * {@link PipelineFactory} at runtime to deduce which interceptors are to be applied for a specific request. <br/>
 *
 * An interceptor pipeline follows the
 * <a href="http://en.wikipedia.org/wiki/Chain-of-responsibility_pattern">Chain of Responsibility</a> pattern and hence
 * every interceptor in the chain has the responsibility of invoking the next interceptor in the pipeline using
 * {@link InterceptorExecutionContext#executeNextInterceptor(FullHttpRequest, HttpResponseWriter)}. Failure to do so, will break
 * the chain and hence the processing of the request. <br/>
 * All interceptors are invoked before karyon invokes the configured {@link HttpRequestRouter}. <br/>
 * Since, these interceptors are invoked in a chain, before execution - after execution interception can be done before
 * and after invoking the next interceptor in the chain. This is the reason, the interceptors do not define the direction of
 * interception as such. <br/>
 * The configured router, conceptually, is always the end of this interceptor chain and hence is invoked after the last
 * interceptor in the chain calls {@link InterceptorExecutionContext#executeNextInterceptor(FullHttpRequest, HttpResponseWriter)}.
 */
package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.interceptor.InterceptorExecutionContext;
import com.netflix.karyon.server.http.spi.HttpRequestRouter;