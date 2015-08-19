package com.netflix.karyon.health;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Singleton;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

/**
 * Registry of {@link HealthIndicator}s that derives all active {@link HealthIndicator}s from Guice bindings.  
 * Any binding to {@link HealthIndicator} is automatically added to the list of active health checks.  Note that 
 * {@link HealthIndicator}s should be registered using set multibindings but ALL binding to HealthIndicator will
 * be picked up here.  Named bindings and mapbindings will also be added but only the name returned from 
 * {@link HealthIndicator#getName()} will be used.
 * 
 * See HealthIndicatorRegsitry for more details on creating a curated list of {@link HealthIndcator}s
 * </pre>
 * 
 * @author elandau
 *
 */
@Singleton
public class AllHealthIndicatorRegistry implements HealthIndicatorRegistry {
    private CopyOnWriteArrayList<HealthIndicator> indicators = new CopyOnWriteArrayList<HealthIndicator>();
    
    @Inject
    public AllHealthIndicatorRegistry(Injector injector) {
        for (Binding<HealthIndicator> binding : injector.findBindingsByType(TypeLiteral.get(HealthIndicator.class))) {
            indicators.add(binding.getProvider().get());
        }
    }
    
    @Override
    public List<HealthIndicator> getHealthIndicators() {
        return Collections.unmodifiableList(indicators);
    }
}
