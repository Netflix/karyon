TCP pipeline
======

It is a simple example of how to run multiple Karyon TCP servers.
There are two servers created:
* frontend, that reads user input and sends it an internal queue
* backend, that reads messages from the internal queue and sends them out to a user

Running the example
===================

`../gradlew runTcpPipelineApp`

What to see
===========

* _Message pipeline_

After the server starts, connect with telnet to frontend and backend servers:
* frontend:```telnet localhost 7770```
* backend: ```telnet localhost 7771```

The text entered on first client, should be print out on the second one.  

* _Admin console_ 

Use the URL: `http://localhost:8077/` to navigate to karyon's admin console.

This will take you to the karyon admin user interface.
