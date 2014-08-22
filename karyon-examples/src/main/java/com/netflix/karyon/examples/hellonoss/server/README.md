Hello Netflix OSS
======

Two simple HTTP server examples using jersey and rx style ```HttpRequestRouter```. This showcases the following components:

* _[karyon-core](../../karyon-core)_ HTTP request router API 
* _[karyon-jersey-blocking](../../karyon-jersey-blocking) extension_: For jersey based resources. 
* _eureka_: Uncomment addition of `KaryonEurekaModule` in the classes [JerseyHelloWorldApp](src/main/java/com/netflix/hellonoss/server/jersey/JerseyHelloWorldApp.java)
 and [RxRouterHelloWorldApp](src/main/java/com/netflix/hellonoss/server/rxrouter/RxRouterHelloWorldApp.java)
 and update [eureka-client.properties](src/main/resources/eureka-client.properties), available in this example, with proper eureka endpoints in your environment.
* _[karyon-admin-web](../../karyon-admin-web)_: Starts an embedded jetty server having an admin console available at http://localhost:8077/
* _karyon health check_: Provides a "always healthy" handler where the implementor can add any logic to signify health of
the application. The handler class is: [HealthCheck](src/main/java/com/netflix/hellonoss/server/health/HealthCheck.java)
* _Karyon Transport_: [karyon-jersey-blocking](../../karyon-jersey-blocking) module uses [RxNetty](https://github.com/Netflix/RxNetty) as the transport layer. 

Running the example
===================

Jersey example:
`../gradlew runJerseyHelloNOSS`


```HttpRequestRouter``` example:
`../gradlew runRxRouterHelloNOSS`

What to see
===========

After the server starts, you can use the following commands to invoke the Jersey or ```HttpRequestRouter``` _Hello world_ resource:

* _Hello to 'newbee'_:

```
 curl -H"MY-USER-ID: nkant"  "http://localhost:8888/hello/to/newbee"
```
Which will produce the output:
```
{"Message":"Hello newbee from Netflix OSS"}
```

* _Hello to Anonymous_:

```
 curl -H"MY-USER-ID: nkant"  "http://localhost:8888/hello/"
```
Which will produce the output:
```
{"Message":"Hello from Netflix OSS"}
```

* _Hello to Healthcheck_:

```
 curl -H"MY-USER-ID: nkant"  "http://localhost:8888/hello/healthcheck"
```
Which will produce the output:
```
{"Status":200}
```

This demonstrates injection in jersey resources as the jersey resource here injects the healthcheck handler.

* _Admin console_ 

Use the URL: `http://localhost:8077/` to navigate to karyon's admin console.

This will take you to the karyon admin user interface.
