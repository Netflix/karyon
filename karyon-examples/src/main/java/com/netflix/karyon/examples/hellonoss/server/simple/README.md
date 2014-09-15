Hello Netflix OSS (Simple Routing)
======

This helloworld example demonstrates how to use karyon's simple but powerful [SimpleUriRouter](https://github.com/Netflix/karyon/blob/master/karyon-core/src/main/java/com/netflix/karyon/transport/http/SimpleUriRouter.java)
to provide HTTP endpoints.

This module demonstrates how one can use karyon's annotation based bootstrapping and guice modules based bootstrapping 
as provided by [karyon-governator](https://github.com/Netflix/karyon/tree/master/karyon-governator)

Running the example
===================

This example can be run using gradle.

* _Annotation based bootstrapping_:

`../gradlew runSimpleRouterAnnotationHelloNOSS`

* _Guice modules based bootstrapping_:

`../gradlew runSimpleRouterGuiceHelloNOSS`

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
