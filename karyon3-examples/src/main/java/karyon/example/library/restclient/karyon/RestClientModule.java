package karyon.example.library.restclient.karyon;

import karyon.example.library.restclient.api.RestClientFactory;
import karyon.example.library.restclient.impl.RestClientFactoryCloud;
import karyon.example.library.restclient.impl.RestClientFactoryDev;

import com.google.inject.AbstractModule;
import com.netflix.karyon.conditional.annotations.ConditionalOnProfile;
import com.netflix.karyon.conditional.annotations.ProvidesConditionally;

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
    
    @Override
    protected void configure() {
    }
    
    // Guice needs this to de-dup modules that installed multiple times
    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }

    // Guice needs this to de-dup modules that installed multiple times
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
