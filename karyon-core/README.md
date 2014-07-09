### karyon-core

This module provides the core functionality of karyon over [RxNetty](https://github.com/Netflix/RxNetty).

#### Health

One of the __best practices__ for an application inside netflix is to advertise its health to the infrastructure.

This health status is used by [Eureka](https://github.com/Netflix/eureka) and hence [Ribbon](https://github.com/Netflix/ribbon)
 to identify unavailable instances.
 
 This module provides the __contract__ for this healthcheck as [HealthCheckHandler](karyon-core/src/main/java/com/netflix/karyon/health/HealthCheckHandler.java)

Karyon's bootstrapping module `karyon-governator` integrates this healthcheck with eureka.

#### Transport

Karyon uses [RxNetty](https://github.com/Netflix/RxNetty) as its transport layer and adds the following functionality:

##### Servo integration.

Configures RxNetty to use the `rx-netty-servo` module for all servers created by karyon.

This does __not__ configure the clients created by RxNetty to use servo. This has to be done by the client library.
 
##### Interceptors

Karyon adds a concept of interceptors (analogus to servlet filters) that provides a way to add cross-cutting 
functionality for all requests.

/// Add the diagram

###### Attaching interceptors

All interceptors are asynchronous.