package com.netflix.karyon.ws.rs;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.FlatResponseOperator;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.ResponseHolder;
import io.reactivex.netty.protocol.http.server.HttpServer;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import rx.functions.Action0;
import rx.functions.Func1;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.karyon.ws.rs.guice.GuiceInjectionSpi;

public class RouterTest {
    @Test
    public void test() throws InterruptedException, ExecutionException, TimeoutException {
        Injector injector = LifecycleInjector.builder()
                .build()
                .createInjector();

        RoutingRequestHandler.Builder builder = RoutingRequestHandler.builder();
        PackagesRouterConfigurer.builder()
            .withInjectionSpi(new GuiceInjectionSpi(injector))
            .withPackages(HelloWorldResource.class.getPackage().getName())
            .build()
            .configure(builder);
        
        HttpServer<ByteBuf, ByteBuf> server = RxNetty.createHttpServer(0, new WsRsRequestHandler(builder.build()))
                .start();
        
        final CountDownLatch finishLatch = new CountDownLatch(1);
        HttpClientResponse<ByteBuf> response = RxNetty.createHttpClient("localhost", server.getServerPort())
                                                      .submit(HttpClientRequest.createGet("/v1/hello/Joe"))
                                                      .lift(FlatResponseOperator.<ByteBuf>flatResponse())
                                                       .map(new Func1<ResponseHolder<ByteBuf>, HttpClientResponse<ByteBuf>>() {
                                                           @Override
                                                           public HttpClientResponse<ByteBuf> call(ResponseHolder<ByteBuf> holder) {
                                                               System.out.println("Got response : " + holder.getContent().toString(Charset.defaultCharset()));
                                                               System.out.println("========================");
                                                               return holder.getResponse();
                                                           }
                                                       })
                                                      .finallyDo(new Action0() {
                                                          @Override
                                                          public void call() {
                                                              finishLatch.countDown();
                                                          }
                                                      }).toBlocking().toFuture().get(10, TimeUnit.SECONDS);
        
    }
}
