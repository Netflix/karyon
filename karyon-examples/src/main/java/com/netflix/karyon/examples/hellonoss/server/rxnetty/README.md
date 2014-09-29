Hello Netflix OSS (RxNetty)
======

This helloworld example demonstrates how to directly use an [RxNetty](https://github.com/ReactiveX/RxNetty) 
`RequestHandler` inside karyon and still leverage all bootstrapping facilities of karyon.

Running the example
===================

This example can be run using gradle.

`../gradlew runPureRxNettyHelloNOSS`

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
