package netflix.karyon.eureka;

import com.google.inject.AbstractModule;
import com.google.inject.binder.LinkedBindingBuilder;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.providers.CloudInstanceConfigProvider;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.discovery.EurekaNamespace;
import com.netflix.discovery.providers.DefaultEurekaClientConfigProvider;
import com.netflix.governator.guice.BootstrapModule;
import netflix.karyon.Karyon;

/**
 * Initialize Eureka, by binding the &quot;eureka.&quot; namespace and adding providers for EurekaClientConfig
 * and EurekaInstanceConfig.
 *
 * @author Nitesh Kant
 */
public class KaryonEurekaModule extends AbstractModule {

    private final String eurekaNamespace;

    public KaryonEurekaModule() {
        this("eureka.");
    }

    public KaryonEurekaModule(String eurekaNamespace) {
        this.eurekaNamespace = eurekaNamespace;
    }

    @Override
    protected void configure() {
        bind(com.netflix.appinfo.HealthCheckHandler.class).to(EurekaHealthCheckHandler.class);
        bind(ApplicationInfoManager.class).asEagerSingleton();
        bind(DiscoveryClient.class).asEagerSingleton();

        configureEureka();
    }

    protected void configureEureka() {
        bindEurekaNamespace().toInstance(eurekaNamespace);
        bindEurekaInstanceConfig().toProvider(CloudInstanceConfigProvider.class);
        bindEurekaClientConfig().toProvider(DefaultEurekaClientConfigProvider.class);
    }

    protected LinkedBindingBuilder<EurekaInstanceConfig> bindEurekaInstanceConfig() {
        return bind(EurekaInstanceConfig.class);
    }

    protected LinkedBindingBuilder<EurekaClientConfig > bindEurekaClientConfig() {
        return bind(EurekaClientConfig.class);
    }

    protected LinkedBindingBuilder<String> bindEurekaNamespace() {
        return bind(String.class).annotatedWith(EurekaNamespace.class);
    }

    public static BootstrapModule asBootstrapModule() {
        return Karyon.toBootstrapModule(KaryonEurekaModule.class);
    }

}
