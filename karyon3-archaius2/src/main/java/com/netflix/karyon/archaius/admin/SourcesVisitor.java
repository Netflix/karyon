package com.netflix.karyon.archaius.admin;

import java.util.Map;
import java.util.Stack;

import com.google.common.base.Joiner;
import com.netflix.archaius.Config;
import com.netflix.archaius.config.CompositeConfig;

public class SourcesVisitor implements Config.Visitor, CompositeConfig.CompositeVisitor {
    private final Map<String, String> sources;
    private final String key;
    private final Stack<String> stack = new Stack<>();
    
    public SourcesVisitor(String key, Map<String, String> sources) {
        this.key = key;
        this.sources = sources;
    }
    
    @Override
    public void visit(Config config, String key) {
        if (this.key.equals(key)) {
            sources.put(Joiner.on("/").join(stack), (String)config.getRawProperty(key));
        }
    }

    @Override
    public void visit(String name, Config child) {
        stack.push(name);
        child.accept(this);
        stack.pop();
    }
}
