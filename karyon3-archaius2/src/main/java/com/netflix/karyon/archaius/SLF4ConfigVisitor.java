package com.netflix.karyon.archaius;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.Config;
import com.netflix.archaius.config.CompositeConfig;

public class SLF4ConfigVisitor implements Config.Visitor, CompositeConfig.CompositeVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(SLF4ConfigVisitor.class.getSimpleName());
    private String prefix = "";
    
    @Override
    public void visit(Config config, String key) {
        LOG.debug(prefix + key + " = " + config.getString(key));
    }

    @Override
    public void visit(String name, Config child) {
        LOG.debug(prefix + "Config: " + name);
        prefix += "  ";
        child.accept(this);
        prefix = prefix.substring(0, prefix.length()-2);
    }
}
