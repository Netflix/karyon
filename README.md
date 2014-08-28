karyon
======

[![Build Status](https://netflixoss.ci.cloudbees.com/job/karyon-master/badge/icon)](https://netflixoss.ci.cloudbees.com/job/karyon-master/)

Karyon in the context of molecular biology is essentially "a part of the cell containing DNA and RNA and responsible for growth and reproduction."

At Netflix, Karyon is a framework and library that essentially contains the blueprint of what it means to implement a cloud ready web service.  All the other fine grained web services and applications that form our SOA graph can essentially be thought as being cloned from this basic blueprint.

Karyon can be thought of as a nucleus that contains the following main ingredients.

* Bootstrapping , dependency and Lifecycle Management (via [Governator](https://github.com/Netflix/governator))
* Runtime Insights and Diagnostics (via `karyon-admin-web` module)
* Configuration Management (via [Archaius](https://github.com/Netflix/archaius))
* Service discovery (via [Eureka](https://github.com/Netflix/eureka))
* Powerful transport module (via [RxNetty](https://github.com/Netflix/RxNetty))

Getting Started
===============

Easiest way to get started with karyon is to see the examples provided with karyon under karyon-examples module.
[This] (https://github.com/Netflix/karyon/tree/master/karyon-examples) is a simple "hello world" application
built using karyon.

Documentation
==============

Please see [wiki] (https://github.com/Netflix/karyon/wiki) for detailed documentation.

Have a question?
===============

We have a discussion group for karyon users (i.e. you!) here: https://groups.google.com/forum/?fromgroups#!forum/karyon-users
