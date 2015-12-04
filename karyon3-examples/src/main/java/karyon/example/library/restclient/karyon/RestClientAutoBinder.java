package karyon.example.library.restclient.karyon;

import javax.inject.Inject;
import javax.inject.Provider;

import karyon.example.library.restclient.api.RestClient;
import karyon.example.library.restclient.api.RestClientFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.netflix.karyon.KeyMatchers;
import com.netflix.karyon.spi.AbstractNamedAutoBinder;

public class RestClientAutoBinder extends AbstractNamedAutoBinder<RestClient> {
    public RestClientAutoBinder() {
        super(KeyMatchers.ofType(RestClient.class));
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
