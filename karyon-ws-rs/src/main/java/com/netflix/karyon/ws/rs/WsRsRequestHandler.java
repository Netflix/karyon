package com.netflix.karyon.ws.rs;

import com.netflix.karyon.ws.rs.rx.RxUtil;

import rx.Observable;
import rx.functions.Func1;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

/**
 * Top level RxNetty RequestHandler that bridges a 
 * 
 * @author elandau
 *
 */
public class WsRsRequestHandler implements io.reactivex.netty.protocol.http.server.RequestHandler<ByteBuf, ByteBuf> {
    private RequestHandler root;
    
    public WsRsRequestHandler(RequestHandler root) {
        this.root = root;
    }
    
    @Override
    public Observable<Void> handle(HttpServerRequest<ByteBuf> request, final HttpServerResponse<ByteBuf> response) {
        RequestContext context = new RequestContext(request, response);
        return root
            .call(context)
            .doOnError(RxUtil.error("Failed to process message"))
            .flatMap(new Func1<Object, Observable<Void>>() {
                @Override
                public Observable<Void> call(Object t1) {
                    return response.writeStringAndFlush(t1.toString());
                }
            });
    }
}
