Hello Netflix OSS
======

A simple helloworld example demonstrating various ways in which to write a karyon server.

All the flavors demonstrate the following karyon components:

* _[karyon-core](../../karyon-core)_ HTTP request router API 
* _eureka_: Uncomment addition of `KaryonEurekaModule` in the respective startup class you are using 
 and update [eureka-client.properties](src/main/resources/eureka-client.properties), available in this example, with proper eureka endpoints in your environment.
* _[karyon-admin-web](../../karyon-admin-web)_: Starts an embedded jetty server having an admin console available at http://localhost:8077/
* _[karyon-governator](../../karyon-governator)_: Uses karyon's governator module for bootstrapping.
* _[karyon-servo](../../karyon-servo)_: Uses karyon's servo module for metrics.
* _karyon health check_: Provides a "always healthy" handler where the implementer can add any logic to signify health of
the application. The handler class is: [HealthCheck](src/main/java/com/netflix/hellonoss/common/health/HealthCheck.java)

See specific example's README file to see details of any additional module used by that example.

* [Jersey](src/main/java/com/netflix/hellonoss/server/jersey)
* [Simple Routing](src/main/java/com/netflix/hellonoss/server/simple)
* [RxNetty](src/main/java/com/netflix/hellonoss/server/rxnetty)

Running the example
===================

The examples can be run using gradle. The following are the respective targets:

* [Jersey](src/main/java/com/netflix/hellonoss/server/jersey): 

```
$ ../gradlew runJerseyHelloNOSS
```

* [Simple Routing](src/main/java/com/netflix/hellonoss/server/simple)

```
$ ../gradlew runSimpleRouterHelloNOSS
```

* [RxNetty](src/main/java/com/netflix/hellonoss/server/rxnetty)

```
$ ../gradlew runPureRxNettyHelloNOSS
```


What to see
===========

After the server starts, you can use the following commands to verify the server's endpoints:

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

* _Healthcheck_:

```
 curl -v "http://localhost:8888/healthcheck"
```
Which will produce the output:
```
> GET /healthcheck HTTP/1.1
> User-Agent: curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8y zlib/1.2.5
> Host: localhost:8888
> Accept: */*
> 
< HTTP/1.1 200 OK
< Content-Type: application/json
< Transfer-Encoding: chunked
< 
```

This demonstrate the healthcheck endpoint added by this application and using karyon's `HealthCheckHandler` contract.

* _Admin console_ 

Use the URL: `http://localhost:8077/` to navigate to karyon's admin console.

This will take you to the karyon admin user interface.
