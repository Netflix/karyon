The RestClient example can be used as a Getting Started guide for writing a library using Karyon.
For this example we will create a simple RestClient API that can be auto-bound and will use different
implementations based on the environment in which it is running (dev or cloud)

# Step 1.  Define your library API

The library API defined as a set of interfaces.  For this example we have the RestClient and a RestClientFactory
for creating client instances.  

```java
public interface RestClient {
    String get(String url);
}
```

```java
public interface RestClientFactory {
    RestClient getClientForService(String serviceName);
}
```

# Step 2.  Specify the bindings

An implementation for RestClientFactory is specified using bindings in a Guice module.  For this example
we are providing two implementations, one for use in local development and one for running in the cloud.
These bindings are conditional on running in either the 'dev' or 'cloud' profile.  The module can be 
installed directly when creating a Karyon injector or automatically via AutoBinding.

```java
public class RestClientModule extends AbstractModule {
    @ProvidesConditionally
    @ConditionalOnProfile("dev")
    RestClientFactory getLocalDevFactory() {
        return new RestClientFactoryDev();
    }
    
    @ProvidesConditionally(isDefault=true)
    @ConditionalOnProfile("cloud")
    RestClientFactory getCloudFactory() {
        return new RestClientFactoryCloud();
    }
}
```

# Step 3.  Define an AutoBinder

To get a RestClient the user could either inject RestClientFactory or a named RestClient.  The named RestClient
is preferable as that would allow for a specific RestClient implementation to be changed outside of the scope of the 
single RestClientFactory.  To avoid having to write boilerplate code for RestClient bindings we define an AutoBinder
that will create a RestClient based on injection points such as @Named("ServiceName") RestClient.  To this we define 
the RestClientAutoBinder class and reference it using the ServiceLoader.

```java
public class RestClientAutoBinder extends AbstractNamedAutoBinder<RestClient> {
    public RestClientAutoBinder() {
        super(TypeLiteralMatchers.subclassOf(RestClient.class));
    }
    
    @Override
    protected Module configure(Key<RestClient> key, String name) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                // The auto-binder needs bindings specified in RestClientModule
                install(new RestClientModule());
                
                bind(key).toProvider(new Provider<RestClient>() {
                    @Inject
                    private RestClientFactory factory;
                    
                    @Override
                    public RestClient get() {
                        return factory.getClientForService(name);
                    }
                });
            }
        };
    }
}
```

/META-INF/service/com.netflix.karyon.spi.AutoBinder
```java
karyon.example.library.restclient.karyon.RestClientAutoBinder
```

# Step 4.  Using the library
To use the library we only need to add the build dependency on the library and inject a named RestClient.

```java
@Singleton
public class ApplicationUsingRestClient {
    public static void main(String args[]) throws ConfigException {
        Karyon.newBuilder()
            .startWithClass(ApplicationUsingRestClient.class, args);
    }
    
    @Inject
    public ApplicationUsingRestClient(@Named("ServiceA") RestClient client) {
        String response = client.get("/home");
    }
}
```