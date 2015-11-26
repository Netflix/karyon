package karyon.example.library.restclient.sampleapp;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import karyon.example.library.restclient.api.RestClient;

import com.netflix.archaius.annotations.Configuration;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.exceptions.ConfigException;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.archaius.ArchaiusKaryonModule;

@Singleton
public class ApplicationUsingRestClient {

    // Example configuration for this application with 
    // each method corresponding to a property with 'foo.' prefix
    @Configuration(prefix = "foo")
    public interface AppConfig {
        String requestPath();
    }
    
    public static void main(String args[]) throws ConfigException {
        Karyon
            // Makes 'ApplicationUsingRestClient' an eager singleton
            .forClass(ApplicationUsingRestClient.class)
            // Enable Archaius2 configuration loading
            .addModules(new ArchaiusKaryonModule()
                    .withApplicationOverrides(MapConfig.builder()
                        .put("foo.requestPath", "home")
                        .build()))
            .start(args);
    }
    
    // @Named("ServiceA") RestClient client and AppConfig config will be auto-bound 
    @Inject
    public ApplicationUsingRestClient(@Named("ServiceA") RestClient client, AppConfig config) {
        String response = client.get(config.requestPath());
        
        System.out.println(response);
    }
}
