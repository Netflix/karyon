karyon-governator
======

This module provides bootstrapping for karyon using [Governator](https://github.com/Netflix/governator)

##### @KaryonBootstrap

This annotation is used to provide an application name and does the necessary bindings using [KaryonBootstrapSuite](src/main/java/com/netflix/karyon/KaryonBootstrapSuite.java)

#### AbstractHttpModule

[AbstractHttpModule](src/main/java/com/netflix/karyon/transport/http/AbstractHttpModule.java) provides an abstract 
module for karyon extension to create a karyon based HTTP server.

#### KaryonServer

[AbstractHttpModule](src/main/java/com/netflix/karyon/KaryonServer.java) is the startup class which has a main method that uses a bootstrap class (annotated with above annotations) to start a Karyon server

See the [hello world example](../karyon-examples/hello-netflix-oss) for a sample.