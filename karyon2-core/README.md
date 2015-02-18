### karyon-core

This module provides the core functionality of karyon over [RxNetty](https://github.com/Netflix/RxNetty).

#### Health

One of the __best practices__ for an application inside netflix is to advertise its health to the infrastructure.

This health status is used by [Eureka](https://github.com/Netflix/eureka) and hence [Ribbon](https://github.com/Netflix/ribbon)
 to identify unavailable instances.
 
 This module provides the __contract__ for this healthcheck as [HealthCheckHandler](karyon2-core/src/main/java/netflix/karyon/health/HealthCheckHandler.java)

Karyon's bootstrapping module `karyon-governator` integrates this healthcheck with eureka.

#### Transport

Karyon uses [RxNetty](https://github.com/Netflix/RxNetty) as its transport layer. 
A karyon based server can be created using the factory class [KaryonTransport](src/main/java/netflix/karyon/transport/KaryonTransport.java)

```

        KaryonTransport.newHttpServer(8888, new HttpRequestRouter<Object, Object>() {
            @Override
            public Observable<Void> route(HttpServerRequest<Object> request, HttpServerResponse<Object> response) {
                return Observable.empty();
            }
        });

```

##### Servo integration.

Configures RxNetty to use the `rx-netty-servo` module for all servers created by karyon.

This does __not__ configure the clients created by RxNetty to use servo. This has to be done by the client library.
 
##### Interceptors

Karyon adds a concept of interceptors (analogus to servlet filters) that provides a way to add cross-cutting 
functionality for all requests.

<img src="https://raw.githubusercontent.com/Netflix/karyon/master/images/Interceptors-Flow.jpg" width="860" height="260">

All interceptors are asynchronous.

###### Attaching interceptors

Interceptors can be attached to keys, which are evaluated for every invocation to determine whether that interceptor is 
applicable or not.

####### Generic Transport

```java

        InterceptorSupport<ByteBuf, ByteBuf, KeyEvaluationContext> support =
                new InterceptorSupport<ByteBuf, ByteBuf, KeyEvaluationContext>();
        support.forKey(new InterceptorKey<ByteBuf, KeyEvaluationContext>() {
            @Override
            public boolean apply(ByteBuf request, KeyEvaluationContext context) {
                return true;
            }
        }).intercept(new DuplexInterceptor<ByteBuf, ByteBuf>() {
            @Override
            public Observable<Void> in(ByteBuf request, ByteBuf response) {
                return Observable.empty();
            }

            @Override
            public Observable<Void> out(ByteBuf response) {
                return Observable.empty();
            }
        });

```

####### HTTP

```java

        HttpInterceptorSupport<ByteBuf, ByteBuf> support = new HttpInterceptorSupport<ByteBuf, ByteBuf>();
        support.forUri("/*").intercept(new DuplexInterceptor<HttpServerRequest<ByteBuf>, HttpServerResponse<ByteBuf>>() {
            @Override
            public Observable<Void> in(HttpServerRequest<ByteBuf> request, HttpServerResponse<ByteBuf> response) {
                return Observable.empty();
            }

            @Override
            public Observable<Void> out(HttpServerResponse<ByteBuf> response) {
                return Observable.empty();
            }
        });

```

After the binding of interceptor is created, the interceptors can be executed using an [InterceptorExecutor](src/main/java/netflix/karyon/transport/interceptor/InterceptorExecutor.java)

This of course is not that useful without attaching to a server, which the out of box provided karyon servers provide.

