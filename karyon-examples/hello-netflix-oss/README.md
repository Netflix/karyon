hello-netflix-oss
======

This is the simplest example of karyon using guice and jersey. This showcases, the following components:

* karyon-extensions: Uses guice extension.
* eureka: This is disabled by default but can be enabled by setting property "com.netflix.karyon.eureka.disable" to "false"
 in hello-netflix-oss.properties or commenting out that property. This will mean populating the property file
 eureka-client.properties, available in this example, with proper eureka endpoints in your environment.
* karyon-admin-web: Starts an embedded jetty server having an admin console available at http://localhost:8077/
* karyon health check: Provides a "always healthy" handler where the implementor can add any logic to signify health of
the application. The handler class is: com.netflix.hellonoss.server.health.HealthCheck

Running the example
===================

There are two ways of using this example, as is,

* Using gradle's jetty plugin: Run the command: ./gradlew :karyon-examples:hello-netflix-oss:jettyRun from the base
directory of karyon (not from the hello-netflix-oss module)

* Create a war file and deploy in the container of your choice: Run the command: ./gradlew :karyon-examples:hello-netflix-oss:war from the base
  directory of karyon (not from the hello-netflix-oss module). This will create the war file in
  the directory: karyon-examples/hello-netflix-oss/build/libs/


What to see
===========

When running with gradle's jetty plugin, you should be able to hit the endpoints:

* http://localhost:8989/hello-netflix-oss/rest/v1/hello/to/newbee: This will give you a response:

{"Message":"Hello newbee from Netflix OSS"}

* http://localhost:8989/hello-netflix-oss/rest/v1/hello:  This will give you a response:

{"Message":"Hello Netflix OSS component!"}

* http://localhost:8077/

This will take you to the karyon admin user interface.