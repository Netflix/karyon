package com.netflix.karyon.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.ProvisionMetrics;
import com.netflix.governator.ProvisionMetrics.Element;
import com.netflix.governator.ProvisionMetrics.Visitor;

@Singleton
@AdminService(name="di-provision", index="list")
public class DIProvisionResource {
    private final ProvisionMetrics metrics;

    @Inject
    public DIProvisionResource(ProvisionMetrics metrics) {
        this.metrics = metrics;
    }
    
    public static class Node {
        private final String name;
        private final List<Node> children;
        private final long value;
        
        public Node(String name, List<Node> children, long value) {
            this.name = name;
            this.children = children;
            this.value = value < 0 ? 0 : value;
        }
        public String getName() {
            return name;
        }
        public List<Node> getChildren() {
            return children;
        }
        public long getValue() {
            return value;
        }
    }
    
    public Node list() {
        final List<Node> result = new ArrayList<>();
        final Stack<List<Node>> stack = new Stack<>();
        stack.push(result);
        
        metrics.accept(new Visitor() {
            @Override
            public void visit(Element entry) {
                final List<Node> children = new ArrayList<Node>();
                stack.peek().add(new Node(entry.getKey().toString(), children, entry.getTotalDuration(TimeUnit.MILLISECONDS)));
               
                stack.push(children);
                entry.accept(this);
                stack.pop();
            }
        });
        long duration = 0;
        for (Node node : result) {
            duration += node.getValue();
        }
        return new Node("app", result, duration);
    }
}
