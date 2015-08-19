Karyon3
---------
Karyon3 is an application integration framework focused on bootstrapping an application using netflix OSS such as Governator, Archaius, Eureka and RxNetty.  Karyon3 makes use of dependency injection (specifically using Google Guice) with Goveranator's context based conditional module loading to transparently load bindings and configurations for the environment in which the application is running.  Karyon3 is broken up into sub-projects on functional and dependency boundaries to reduce pulling in excessive dependencies.  

Core features
* Minimize dependencies
* Context based auto-configuration
* Health check
* Admin console
* Integration with core Netflix OSS

----------
Getting Started
-------------------
Karyon is available on maven central

```java
compile "com.netflix.karyon:karyon3-core:3.0.1-rc.29'
```

----------
Main
-------
A Karyon3 based main should normally consist of a single line of code to create the injector via Karyon, given a configuration and a set of modules, plus block on the application to terminate (this can be made event drive).   Karyon encourages a clear separation between the injector creation, binding specification (via Guice modules) and code which should only use the JSR330 annotations. 

```java
public class HelloWorld {
    public static void main(String[] args) {
        Karyon.createInjector(
            // 
            ArchaiusKaryonConfiguration.createDefault(),
            // Add any guice module
            new ArchaiusLog4J2ConfigurationModule(),
            new AdminServerModule(),
            new AdminUIServerModule(),
            new ArchaiusModule(),
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

First, add a dependency on governator-servlet

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
Running in Jetty
----------------------

----------
Application Lifecycle
--------------------------

----------
Conditional module loading
----------------------------------
Karyon makes use of Governator's conditional module loading to auto install Guice modules based on the application runtime environment.  For example, if the application is using Eureka and running in EC2 Karyon will auto-load the configuration necessary to connect to Eureka in EC2.

TODO: see Governator Conditional Loading Documentation

----------
Archaius Configuration
----------------------------

----------
Admin Console
-------------------
The admin console provides invaluable insight into the internal state of a running instance.  To minimize dependencies needed to run the admin console Karyon uses a naming convention based routing to expose Pojo's as REST endpoints using the Oracle JDK built in web server.  

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

### Writing a custom REST endpoint
TODO
### Writing a custom UI page
TODO

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

### Using HealthCheck
@Path("/health")
public class HealthCheckResource {
    @Inject
    public HealthCheckResource(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

   @GET
   public HealthCheckStatus doCheck() {
       return healthCheck.check().join();
   }   
}

### Custom health check 
To create a custom health indicator simply implement HealthIndicator and inject any objects that are needed to determine the health state.  
```java
public class MyHealthIndicator implements HealthIndicator {
    @Inject
    public MyHealthIndicator(MyService service) {
        this.service = service;
    }
    
    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        if (service.getErrorRate() > 0.1) {
            return CompletableFuture.completedFuture(HealthIndicatorStatuses.unhealthy(getName()));
        }
        else {
             return CompletableFuture.completedFuture(HealthIndicatorStatuses.healthy(getName()));
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
### Curated health indicator
TBD
### Configuration based health indicator
TBD

----------
Eureka Integration
-----------------------
First, add the following dependency 
```gradle
compile 'com.netflix.karyon:karyon3-eureka:3.0.1-rc.28'
```

Integrates application lifecycle + health check into eureka

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
RxNetty
-----------
TODO

----------
Logging
-----------
TODO