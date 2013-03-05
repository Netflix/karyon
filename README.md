karyon
======

Karyon in the context of molecular biology is essentially "a part of the cell containing DNA and RNA and responsible for growth and reproduction."

At Netflix, Karyon is a framework and library that essentially contains the blueprint of what it means to implement a cloud ready web service.  All the other fine grained web services and applications that form our SOA graph can essentially be thought as being cloned from this basic blueprint.

Today, we would like to announce the general availability of Karyon as part of our rapidly growing NetflixOSS stack. Thus far we have open sourced many components that form the Netflix Cloud Platform, and with the release of Karyon, you now have a way to glue them all together.

Karyon can be thought of as the nucleus of a blueprint that contains the following main ingredients.

* Bootstrapping , Libraries and Lifecycle Management (via NetflixOSS's Governator)
* Runtime Insights and Diagnostics (via built in Admin Console)
* Pluggable Web Resources (via JSR-311 and Jersey)
* Cloud Ready
