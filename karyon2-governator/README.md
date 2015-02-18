karyon-governator
======

This module provides bootstrapping for karyon using [Governator](https://github.com/Netflix/governator). Bootstrapping
is categorized into two broad categories:

#### Annotation Based

This model leverages [Governators](https://github.com/Netflix/governator) `@Bootstrap` annotations to provide syntactic 
 sugar for discovering applicable modules.
Under this model, one provides all `@Bootstrap` annotations on a class/interface which is referred to as an 
_Application_ class and is consumed by Karyon to create a [KaryonServer](src/main/java/netflix/karyon/KaryonServer.java)
  
An example of this model can be found under [karyon examples](../karyon2-examples)

###### KaryonRunner

[KaryonRunner](src/main/java/netflix/karyon/KaryonRunner.java) is a utility class which can be used as the startup
for all _Application_ classes created in this annotation based model. This class has a main method which expects an
_Application_ class name to be passed as the program argument. 

#### Guice modules Based

The annotation based model above leverages this basic guice module based bootstrapping model which requires the user to
explicitly provide the guice modules which are required by the application.

An example of this model can be found under[karyon examples](../karyon2-examples)
