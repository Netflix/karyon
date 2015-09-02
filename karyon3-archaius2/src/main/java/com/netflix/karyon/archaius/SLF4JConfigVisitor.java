package com.netflix.karyon.archaius;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.Config;
import com.netflix.archaius.config.CompositeConfig;

public class SLF4JConfigVisitor implements CompositeConfig.CompositeVisitor<Void> {
    private static final Logger LOG = LoggerFactory.getLogger(SLF4JConfigVisitor.class.getSimpleName());
    private String prefix = "";
    
    @Override
    public Void visit(Config config, String key) {
        LOG.debug(prefix + key + " = " + config.getString(key));
        return null;
    }

    @Override
    public Void visit(String name, Config child) {
        LOG.debug(prefix + "Config: " + name);
        prefix += "  ";
        child.accept(this);
        prefix = prefix.substring(0, prefix.length()-2);
        return null;
    }
}
