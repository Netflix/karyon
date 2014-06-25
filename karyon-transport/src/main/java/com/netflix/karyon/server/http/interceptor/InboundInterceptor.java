package com.netflix.karyon.server.http.interceptor;

import com.netflix.karyon.server.http.spi.HttpRequestRouter;
import com.netflix.karyon.server.spi.ResponseWriter;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;

/**
 * Interceptor that is called by karyon before invoking {@link HttpRequestRouter}.
 *
 * <h2>State sharing</h2>
 * Implementations are encouraged to use netty's state sharing mechanism as defined in {@link ChannelHandler} using
 * {@link ChannelHandlerContext} which can be obtained using {@link ResponseWriter#getChannelHandlerContext()} <br/>
 *
 * A typical Interceptor code will look like:
 <PRE>
     public class InboundInterceptorImpl&lt;I extends HttpObject, O extends HttpObject&gt; implements InboundInterceptor&lt;I, O&gt; {

        {@code @Override}
        public void interceptIn(I httpRequest, ResponseWriter&lt;O&gt; responseWriter, NextInterceptorInvoker&lt;I, O&gt; invoker) {
            try {
                // do your stuff
            } finally {
                invoker.executeNext(httpRequest, responseWriter);
            }
        }
    }

 </PRE>
 *
 *
 * @param <I> Request object type correcponding to the pipeline.
 * @param <O> Response object type correcponding to the pipeline.
 *
 * @see com.netflix.karyon.server.http.interceptor
 *
 * @author Nitesh Kant
 */
public interface InboundInterceptor<I extends HttpObject, O extends HttpObject> {

    /**
     * Executes this interceptor.
     *
     * @param httpRequest Request for which this interceptor is invoked.
     * @param responseWriter Response writer.
     * @param invoker Used to invoke the next interceptor in the pipeline.
     */
    void interceptIn(I httpRequest, ResponseWriter<O> responseWriter, NextInterceptorInvoker<I, O> invoker);

}
