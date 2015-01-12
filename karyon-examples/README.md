Karyon Examples
================

This module provides a catalog for the variety of ways to interact with karyon.

Examples Catalog
================

Category           | Example                                                                                      | Description
-------------------|----------------------------------------------------------------------------------------------|------------
Simple URI Routing | [Annotation based](src/main/java/netflix/karyon/examples/hellonoss/server/simple/annotation)        | A simple URI based routing application that uses karyon's annotation based bootstrapping model.
Simple URI Routing | [Guice module based](src/main/java/netflix/karyon/examples/hellonoss/server/simple/module)          | A simple URI based routing application that uses karyon's guice modules based bootstrapping model.
Jersey             | [Annotation based](src/main/java/netflix/karyon/examples/hellonoss/server/jersey)                   | An example of how to use blocking jersey resources inside karyon.
RxNetty            | [Guice module based](src/main/java/netflix/karyon/examples/hellonoss/server/rxnetty)                | An example of how to directly use an RxNetty based `RequestHandler` inside karyon.
TCP                | [Karyon TCP server](src/main/java/netflix/karyon/examples/tcp)                                      | An example of how to create a raw TCP karyon server.
WebSockets         | [Karyon WebSocket Echo server](src/main/java/netflix/karyon/examples/websockets)                    | An example of WebSockets server.

Build
=====

To build:

```
$ cd karyon/karyon-examples
$ ../gradlew build
```

Run
===

It is possible to run these examples using Gradle. For list of available tasks check "Examples" group
in the gradle task list:

```
$ ../gradlew tasks
```

For example to run the simple URI router example run the following command:

```
$ ../gradlew runSimpleRouterHelloNOSS
```

Some of the examples require parameters. They must be passed with ```-P<name>=<value>``` option. 
