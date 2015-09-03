Karyon3
---------
Karyon3 is a integration framework focused on bootstrapping JVM application using netflix OSS such as Governator, Archaius, Eureka and RxNetty.  Karyon3 makes use of dependency injection (specifically using Google Guice) with Goveranator's context based conditional module loading to transparently load bindings and configurations for the environment in which the application is running.  Karyon3 is broken up into sub-projects on functional and dependency boundaries to reduce pulling in excessive dependencies.  

Core features
- Minimize dependencies
- Context based auto-configuration
- Health check
- Admin console
- Integration with core Netflix OSS

Note that Karyon3 is meant to be container agnostic and can run inside Tomcat, spawn a Jetty or RxNetty server.

----------
Getting Started
-------------------
Karyon is currently available as a snapshot via jfrog.

```java
repositories {
    maven { url 'http://oss.jfrog.org/oss-snapshot-local' }
}
compile "com.netflix.karyon:karyon3-core:3.0.1-SNAPSHOT'
```

----------
Main
-------
A Karyon3 based main should normally consist of a simple block of code to create the injector via Karyon given a configuration and a set of modules, plus block on the application to terminate (this can be made event drive).   Karyon encourages a clear separation between the injector creation, binding specification (via Guice modules) and code which should only use the JSR330 annotations. 

```java
public class HelloWorld {
    public static void main(String[] args) {
        Karyon.createInjector(
            // Default archaius based karyon configuration
            ArchaiusKaryonConfiguration.createDefault(),
            // Add any guice module
            new ApplicationModule())
            // Block until the application terminates
           .awaitTermination();
    }
}
```
----------
Running in Tomcat
-----------------------
To run in tomcat simply extend Governator's GovernatorServletContextListener and create the injector just as you would a standalone application.

First, add a dependency on governator-servlet (TODO: Make this a karyon sub-project)

```gradle
compile "com.netflix.governator:governator-servlet:1.9.3"
```

Next, write your ContextListener
```java
package com.example;

public class MyContextListener extends GovernatorServletContextListener {
    @Override
    protected Injector createInjector() {
        return Karyon.createInjector(
                ArchaiusKaryonConfiguration.createDefault(),
                new EurekaModule(),
                new ArchaiusModule(),
                new ServletModule() {
                    ...
                }
                // ... more modules
        );
    }
```

Finally, reference the context listener in web.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app version="2.5" xmlns="http://java.sun.com/xml/ns/j2ee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <filter>
        <filter-name>guiceFilter</filter-name>
        <filter-class>com.google.inject.servlet.GuiceFilter</filter-class>
    </filter>

    <filter-mapping>
        <filter-name>guiceFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <listener>
        <listener-class>com.example.MyContextListener</listener-class>
    </listener>
</web-app>

```
----------
Running with Jetty
----------------------
```java
public class HelloWorld {
    public static void main(String[] args) {
        Karyon.createInjector(
            // Default archaius based karyon configuration
            ArchaiusKaryonConfiguration.createDefault(),
            // Add any guice module
            new ApplicationModule(),
            <b>new JettyModule()</b>)
            // Block until the application terminates
           .awaitTermination();
    }
}
```

----------
Conditional module loading
----------------------------------
Karyon makes use of Governator's conditional module loading to auto install Guice modules based on the application runtime environment.  Conditionals can depend on property values, modules having been installed, bindings, etc.  An example use case would be to set the appropriate bindings for running Eureka locally as opposed to running in the cloud without requiring the developer to know which specific bindings to override.  For conditional bindings to work all the jars must be in the classpath and the modules made known to Karyon/Governator via a ModuleListProvider (such as ClassPathModuleListProvider, ServiceLoaderModuleListProvider, etc...).  By default karyon will include any modules under the 'com.netflix.karyon' package.

```java
Karyon.createInjector(
    ArchaiusKaryonConfiguration.builder()
        .addModuleListProvider(ModuleListProvides.forPackage("org.example")
        .build(),
    ...);
```

In addition to the conditionals built in to Governator Karyon offers two key conditionals, ConditionalOnLocalDev and ConditionalOnEc2 that can be used to load specific modules (i.e. bindings) for local development and unit tests or when running in an EC2 environment.  

For example,
```java
@ConditionalOnLocalDev
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(Foo.class).to(**FooImpl1.class**);
    }
}

@ConditionalOnLocalDev
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(Foo.class).to(**FooImpl2.class**);
    }
}
```

----------
Archaius Configuration
----------------------------
Karyon3 uses Archaius2 to manage the application configuration.   Archaius provides a simple override structure through which configuration may be loaded and overwritten.  The configuration is fully DI'd and is therefore injectable into any code.  

### Configuration proxy
Archaius encourages the user of java interfaces to model configuration for a class as opposed to depending on a specific configuration API (such as apache commons) or mapping to Pojo setters (makes it difficult to have final fields).  To simplify the use of interfaces Archaius provides a mechanism to bind a proxied implementation to the configuration.  This approach has several benefits, 
*  Typed configuration mapping
*  Decouple configuration representation from configuration format
*  Mockable configuration
*  Decouple configuration from executable code

A configuration interface looks like this,

```java
@Configuration(prefix="foo")  // Optional prefix
@ConfigurationSource("foo")   // Will load foo.properties (and cascade overrides)
public interface FooConfig {
    @DefaultValue("50")
    int getTimeout(); // Will bind to property foo.timeout
}
```

The configuration interfaces is used like this,
```java
public class Foo { 
    @Inject
    Foo(FooConfig config) {
    }
}
```

To create the proxied configuration
``` java
new AbstractModule() {
    @Provides
    @Singleton
    FooConfig getFooConfiguration(ConfigProxyFactory factory) {
        return factory.newProxy(FooConfig.class);
    }
}
```

For more complex configurations that can't be modeled as an interface it is still possible to just inject archaius's Config and access properties manually.

```java
public class Foo {
    @Inject
    Foo(Config config) {
        config.getInteger("foo.timeout", 50);
    }
}
```

### Dynamic configuration 
TODO

### Loading Configuration Files
TODO

### ServerContext
TODO

----------
Application Lifecycle
--------------------------
TODO

----------
Admin Console
-------------------
The admin console provides invaluable insight into the internal state of a running instance.  To minimize dependencies needed to run the admin console Karyon uses convention based routing to expose Pojo's as REST endpoints using the Oracle JDK built in web server.  

To enable the Admin Console REST server (default port 8077)
```java
import com.netflix.karyon.admin.rest.AdminServerModule;
...
new AdminServerModule()
```

To enable the simple UI server (default port 8078)
```java
import com.netflix.karyon.admin.ui.AdminUIServerModule;
...
AdminUIServerModule
```

The REST and Admin ports are completely decoupled so that the UI may be hosted (and modified) remotely.  

```properties
karyon.server.admin.remoteServer=http://org.example.adminserver:80/index.html#/${@publicHostname}:8077/")
```

### Writing a custom REST endpoint
```java
@Singleton
public class MyAdminResource {
   @Inject
   public MyAdminResource() {
   }

   // maps to '/'
   List<SomeEntity> get() {
   }

   // maps to '/:key'
   SomeEntity get(String key) {
   }

   // maps to '/:key/sub'
   List<SomeSubEntity> getSub(String key, String sub) {
   }
}
```

Note that this scheme is very limited and will likely be modified in the future to support a subset of JAX-RS annotations.

```java
@ConditionalOnModule(AdminModule.class)
public MyAdminModule extends AbstractAdminModule {
   @Override
   public void configure() {
       // Will map the root path of the above MyAdminResource to '/my-admin'
       bindAdminResource("my-admin").to(MyAdminResource.class);
   }
}
```
### Writing a custom UI page
By default Karyon3 will display the raw JSON data returned by an admin endpoint.  A custom UI may be added by include a file '/admin/my-admin.html' with your admin jar.  

----------
Health check
----------------
Instance health is an important aspect of any cloud ready application.  It is used for service discovery as well as bad instance termination.  Through Karyon's HealthCheck API an application can expose a REST endpoint for external monitoring to ping for health status or integrate with Eureka for service discovery registration based on instance health state.  An instance can be in one of 4 lifecycle states: Starting, Running, Stopping and Stopped.  HealthCheck state varies slightly in that it combines these application lifecycle states with the instance health to provide the following states: Starting, Healthy, Unhealthy or OutOfService.   

* Starting - the application is healthy but not done bootstrapping
* Healthy - the application finished boostrapping and is functioning propertly
* Unhealthy - the application either failed bootstrapping or is not functioning properly
* OutOfService - the application has been shut down

The HealthCheck API is broken up into several abstractions to allow for maximum customization.  It's important to understand these abstractions.
* HealthIndicator - boolean health indicator for a specific application features/aspect.  For example, CPU usage.
* HealthIndicatorRegistry - registry of all health indicators to consider for health check.   The default HealthIndicatorRegistry ANDs all bound HealthIndicator (MapBinding, Multibinding, Qualified Binding).  Altenatively and application may manually construct a HealthIndicatorRegistry from a curated set of HealthIndicators.
* HealthCheck - combines application lifecycle + indicators to derive a meaningful health state

### Using HealthCheck in a Jersey resource
```java
@Path("/health")
public class HealthCheckResource {
    @Inject
    public HealthCheckResource(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

   @GET
   public HealthCheckStatus doCheck() {
       return healthCheck.check().get();
   }   
}
```

### Custom health check 
To create a custom health indicator simply implement HealthIndicator, inject any objects that are needed to determine the health state, and implement you logic in check().  Note that check returns a future so that the healthcheck system can implement a timeout.  The check() implementation is therefore expected to be well behaved and NOT block.
  
```java
public class MyHealthIndicator extends AbstractHealthIndicator {
    @Inject
    public MyHealthIndicator(MyService service) {
        this.service = service;
    }
    
    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        if (service.getErrorRate() > 0.1) {
            return CompletableFuture.completedFuture(healthy(getName()));
        }
        else {
             return CompletableFuture.completedFuture(healthy(getName()));
        }
    }
 
    @Override
    public String getName() {
        return "MyService";
    }
}
```

To enable the HealthIndicator simply register it as a set binding.  It will automatically be picked up by the default HealthIndicatorRegistry
```java
Multbindings.newSetBinder(binder()).addBinding().to(MyHealthIndicator.class);
```
### Curated health check registry
TBD

### Configuration based health indicator
TBD

----------
Eureka Integration
-----------------------
First, add the following dependency 

```gradle
compile 'com.netflix.karyon:karyon3-eureka:3.0.1-SNAPSHOT'
```

Next add the EurekaModule from OSS eureka-client

```gradle
Karyon.createInjector(
    ArchaiusKaryonConfiguration.createDefault(),
    new EurekaModule(),
    ...
```

Using to conditional loading the Karyon will auto-install a module that will bridge Karyon's health check with Eureka's V2 health check.  

TODO: manually mark instance as UP

----------
Jersey Integration
----------------------
Karyon3 doesn't offer any specific Jersey integration other the then existing Jersey Guice integration.  To add Jersey support,

First, add the following dependency 
```gradle
compile 'com.sun.jersey.contribs:jersey-guice:1.18.1'
```

Then simply add a JerseyServletModule implementation to the list of modules passed to Karyon
```java
Karyon.createInjector(
    ArchaiusKaryonConfiguration.createDefault(),
    new JerseyServletModule() {
        @Override
        protected void configureServlets() {
            serve("/*").with(GuiceContainer.class);
            bind(GuiceContainer.class).asEagerSingleton();
            bind(SomeJerseyClass.class).asEagerSingleton();
        }
    });
```

----------
RxNetty Server
-------------------
NOTE: Karyon currently uses RxNetty 0.4.x until 0.5.x is released.

Karyon provides a mechanism to define and configure multiple RxNetty servers within a single application with servlet style request routing similar to ServletModule.   Based on these bindings Karyon will auto-start the servers as the injector is created.  

To add RxNetty support
```gradle
compile 'com.netflix.karyon:karyon3-rxnetty:3.0.1-SNAPSHOT'
```

To specify basic URL routes for an RxNetty Server
```gradle
Karyon.createInjector(
    ArchaiusKaryonConfiguration.createDefault(),
    new RxNettyServerModule() {
        @Override
        protected void configureEndpoints() {
            serve("/hello").with(HelloWorldRequestHandler.class);
        }
    },
    ...
```

HelloWorldRequestHandler is a standard RxNetty request handler
```java
@Singleton
public class HelloWorldRequestHandler implements RequestHandler<ByteBuf, ByteBuf> {
    @Override
    public Observable<Void> handle(
            HttpServerRequest<ByteBuf> request,
            HttpServerResponse<ByteBuf> response) {
        return response.writeStringAndFlush("Hello World!");
    }
}
```

### Server configuration
Karyon will auto-create a default binding for ServerConfig.  However, an alternate ServerConfig may be provided by specifying the binding to ServerConfig.  

```properties
karyon.httpserver.serverPort=7001
```

### Qualified RxNetty Server
Qualified RxNetty servers makes it possible to expose services (such as admin) over other ports.

```gradle
Karyon.createInjector(
    ArchaiusKaryonConfiguration.createDefault(),
    new RxNettyServerModule() {
        @Override
        protected void configureEndpoints() {
            serve(FooServer.class, "/foo").with(FooRequestHandler.class);
        }
    },
    ...
```

Where the port number is defined in the property 
```properties
karyon.httpserver.serverPort=7001
```
### Raw usage of RxNetty 
If not interested in the built in routing an RxNetty server may be constructed manually using a simple @Provides method.

```java
new AbstractModule() {
    @Provides
    @Singleton
    HttpServer<ByteBuf, ByteBuf> getShutdownServer() {
        return RxNetty.newHttpServerBuilder(
            80, 
            new FooRequestHandler()
            )
            .build();
    }
}
```

----------
Logging
-----------
TODO

Testing
----------
TODO