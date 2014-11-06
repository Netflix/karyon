package netflix.karyon.server.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.protocol.http.UnicastContentSubject;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import netflix.karyon.server.MockChannelHandlerContext;
import netflix.karyon.transport.http.HttpKeyEvaluationContext;
import netflix.karyon.transport.interceptor.InterceptorKey;

/**
 * @author Nitesh Kant
 */
public class InterceptorConstraintTestBase {

    protected static boolean doApplyForGET(InterceptorKey<HttpServerRequest<ByteBuf>, HttpKeyEvaluationContext> key, String uri) {
        return doApply(key, uri, HttpMethod.GET);
    }

    protected static boolean doApply(InterceptorKey<HttpServerRequest<ByteBuf>, HttpKeyEvaluationContext> key, String uri,
                                     HttpMethod httpMethod) {
        DefaultHttpRequest nettyRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_0, httpMethod, uri);
        return key.apply(new HttpServerRequest<ByteBuf>(nettyRequest,
                                                        UnicastContentSubject.<ByteBuf>createWithoutNoSubscriptionTimeout()),
                         new HttpKeyEvaluationContext(new MockChannelHandlerContext("mock").channel()));
    }
}
