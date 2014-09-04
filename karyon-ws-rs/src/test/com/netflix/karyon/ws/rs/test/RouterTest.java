package com.netflix.karyon.ws.rs.test;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.client.FlatResponseOperator;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.ResponseHolder;
import io.reactivex.netty.protocol.http.server.HttpServer;

import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import rx.Observable;
import rx.functions.Func1;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import com.netflix.karyon.ws.rs.WsRsRequestHandler;
import com.netflix.karyon.ws.rs.guice.GuiceIocProviderFactory;
import com.netflix.karyon.ws.rs.router.PackagesRouterConfigurer;
import com.netflix.karyon.ws.rs.router.RoutingRequestHandler;
import com.netflix.karyon.ws.rs.rx.RxUtil;

public class RouterTest {
    public static class ServerResource extends ExternalResource {
        Injector injector;
        HttpServer<ByteBuf, ByteBuf> server;
        
        public void before() throws Throwable {
            this.injector = LifecycleInjector.builder()
                    .build()
                    .createInjector();

            injector.getInstance(LifecycleManager.class).start();
            
            RoutingRequestHandler.Builder builder = RoutingRequestHandler.builder();
            PackagesRouterConfigurer.builder()
                .withIoCProviderFactory(new GuiceIocProviderFactory(injector))
                .withPackages(BaseResourceImpl.class.getPackage().getName())
                .build()
                .configure(builder);
            
            server = RxNetty.createHttpServer(0, new WsRsRequestHandler(builder.build()))
                    .start();
            
        }
        
        public void after() {
            try {
                server.shutdown();
            } catch (InterruptedException e) {
            }
            injector.getInstance(LifecycleManager.class);
        }
        
        public int getServerPort() {
            return server.getServerPort();
        }
        
        public class TestDsl {
            HttpClientRequest<ByteBuf> request;
            final CountDownLatch finishLatch = new CountDownLatch(1);
            
            public TestDsl(HttpClientRequest<ByteBuf> request) {
                this.request = request;
            }
            
            public TestDsl header(String header, String value) {
                request.withHeader(header, value);
                return this;
            }
            
            public TestDsl accepts(MediaType type) {
                request.withHeader(HttpHeaders.Names.ACCEPT, type.toString());
                return this;
            }
            
            public String execute() throws Exception {
                return RxNetty.createHttpClient("localhost", server.getServerPort())
                        .submit(request)
                        .lift(FlatResponseOperator.<ByteBuf>flatResponse())
                        .flatMap(new Func1<ResponseHolder<ByteBuf>, Observable<String>>() {
                             @Override
                             public Observable<String> call(ResponseHolder<ByteBuf> holder) {
                                 if (!holder.getResponse().getStatus().equals(HttpResponseStatus.OK)) {
                                     return Observable.error(new RuntimeException("Bad response : " + holder.getResponse().getStatus()));
                                 }
                                 String response =  holder.getContent().toString(Charset.defaultCharset());
                                 System.out.println("Response: " + response);
                                 System.out.println("========================");
                                 return Observable.just(response);
                             }
                         })
                        .finallyDo(RxUtil.countdown0(finishLatch))
                        .toBlocking()
                        .toFuture()
                        .get(1000, TimeUnit.SECONDS);
                
            }
        }
        
        public TestDsl get(String path) throws Exception {
            return new TestDsl(HttpClientRequest.createGet(path));
        }
    }
    
    @Rule
    public ServerResource server = new ServerResource();
    
    @Test
    public void testString() throws Exception {
        String resp = server.get("/text/str/Joe").execute();
        Assert.assertEquals("str:Joe", resp);
    }
    
    @Test
    public void testInteger() throws Exception {
        String resp = server.get("/text/int/123").execute();
        Assert.assertEquals("int:123", resp);
    }

    @Test
    public void testLong() throws Exception {
        String resp = server.get("/text/long/123").execute();
        Assert.assertEquals("long:123", resp);
    }

    @Test
    public void testDouble() throws Exception {
        String resp = server.get("/text/double/1.2").execute();
        Assert.assertEquals("double:1.2", resp);
    }

    @Test
    public void testHeader() throws Exception {
        String resp = server.get("/text/header").header("foo", "bar").execute();
        Assert.assertEquals("header:bar", resp);
    }

    @Test
    public void testHeaderWithDefault() throws Exception {
        String resp = server.get("/text/header").execute();
        Assert.assertEquals("header:default", resp);
    }

    @Test
    public void testTwo() throws Exception {
        String resp = server.get("/text/two/123/456").execute();
        Assert.assertEquals("two:123:456", resp);
    }
    
    @Test
    public void testJsonObservable() throws Exception {
        String resp = server.get("/json/observable/user?first=John&last=Smith").accepts(MediaType.APPLICATION_JSON_TYPE).execute();
        Assert.assertEquals("{\"first\":\"John\",\"last\":\"Smith\"}", resp);
    }
    
    @Test
    public void testJsonArrayObservable() throws Exception {
        String resp = server.get("/json/observable/users").accepts(MediaType.APPLICATION_JSON_TYPE).execute();
        Assert.assertEquals("[{\"first\":\"John\",\"last\":\"Doe\"},{\"first\":\"Jane\",\"last\":\"Doe\"}]", resp);
    }
    
    @Test
    public void testJson() throws Exception {
        String resp = server.get("/json/user?first=John&last=Smith").accepts(MediaType.APPLICATION_JSON_TYPE).execute();
        Assert.assertEquals("{\"first\":\"John\",\"last\":\"Smith\"}", resp);
    }
    
    @Test
    public void testJsonArray() throws Exception {
        String resp = server.get("/json/users").accepts(MediaType.APPLICATION_JSON_TYPE).execute();
        Assert.assertEquals("[{\"first\":\"John\",\"last\":\"Doe\"},{\"first\":\"Jane\",\"last\":\"Doe\"}]", resp);
    }

}
