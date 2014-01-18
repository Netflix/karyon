package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;

/**
 * Interceptor that is called by karyon after the {@link HttpRequestRouter} has finished processing and initiated a
 * response write back.
 *
 * <h2>State sharing</h2>
 * Implementations are encouraged to use netty's state sharing mechanism as defined in {@link ChannelHandler} using
 * {@link ChannelHandlerContext} which can be obtained using {@link ResponseWriter#getChannelHandlerContext()} <br/>
 *
 * A typical Interceptor code will look like:
 *
 <PRE>
     public class OutboundInterceptorImpl&lt;O extends HttpObject&gt; implements OutboundInterceptor&lt;O&gt; {

     {@code @Override}
     public void interceptOut(O httpResponse, ResponseWriter&lt;O&gt; responseWriter,
                              NextInterceptorInvoker&lt;O&gt; invoker) {
             try {
                // do your stuff
             } finally {
                invoker.executeNext(httpResponse, responseWriter);
             }
         }
     }

 </PRE>
 *
 * @param <O> Response object type correcponding to the pipeline.
 *
 * @see com.netflix.karyon.server.http.interceptor
 *
 * @author Nitesh Kant
 */
public interface OutboundInterceptor<O extends HttpObject> {

    /**
     * Executes this interceptor.
     *
     * @param httpResponse Request for which this interceptor is invoked.
     * @param responseWriter Response writer.
     * @param invoker Used to invoke the next interceptor in the pipeline.
     */
    void interceptOut(O httpResponse, ResponseWriter<O> responseWriter, NextInterceptorInvoker<O, O> invoker);
}
