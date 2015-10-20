package com.netflix.karyon.archaius;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.netflix.karyon.KaryonAutoContext;

@Singleton
public class KaryonCascadeStrategy extends InterpolatingCascadeStrategy {

    private KaryonAutoContext context;

    @Inject
    public KaryonCascadeStrategy(KaryonAutoContext context) {
        this.context = context;
    }
    
    @Override
    protected List<String> getPermuations() {
        List<String> permuatations = new ArrayList<>();
        permuatations.add("%s");
        for (String profile : context.getProfiles()) {
            permuatations.add("%s-" + profile);
        }
        permuatations.addAll(Arrays.asList(
                "%s-${@environment}",
                "${@datacenter}-%s",
                "${@datacenter}-%s-${@environment}"
                ));
        return permuatations;
    }

}
