package com.netflix.karyon.admin.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.netflix.archaius.Config;

public class Interpolator {
    public Interpolator create(StrLookup<String> lookup) {
        return new Interpolator(lookup);
    }

    private List<StrLookup<String>> lookups;
    
    public Interpolator(StrLookup<String> lookup) {
        this.lookups = Arrays.asList(lookup);
    }
    
    public Interpolator(Map<String, String> valueMap) {
        this.lookups = Arrays.asList(StrLookup.<String>mapLookup(valueMap));
    }
    
    public Interpolator(List<StrLookup<String>> lookups) {
        this.lookups = new ArrayList<>(lookups);
    }
    
    private static StrLookup<String> fromConfig(final Config config) {
        return new StrLookup<String>() {
            @Override
            public String lookup(String key) {
                return config.getString(key);
            }
        };
    }
    
    public Interpolator(Config config) {
        this.lookups = Arrays.asList(fromConfig(config));
    }
    
    public Interpolator withFallback(StrLookup<String> lookup) {
        List<StrLookup<String>> lookups = new ArrayList<>(this.lookups);
        lookups.add(lookup);
        return new Interpolator(lookups);
    }
    
    public Interpolator withFallback(Map<String, String> valueMap) {
        List<StrLookup<String>> lookups = new ArrayList<>(this.lookups);
        lookups.add(StrLookup.<String>mapLookup(valueMap));
        return new Interpolator(lookups);
    }
    
    public Interpolator withFallback(Config config) {
        List<StrLookup<String>> lookups = new ArrayList<>(this.lookups);
        lookups.add(fromConfig(config));
        return new Interpolator(lookups);
    }
    
    public String interpolate(String value) {
        return new StrSubstitutor(
              new StrLookup<String>() {
                  @Override
                  public String lookup(String key) {
                      for (StrLookup<String> lookup : lookups) {
                          String value = lookup.lookup(key);
                          if (value != null) {
                              return value;
                          }
                      }
                      return null;
                  }
              }, "${", "}", '$')
            .replace(value);
    }

}
