package com.netflix.karyon.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.netflix.governator.ProvisionMetrics;
import com.netflix.governator.ProvisionMetrics.Element;
import com.netflix.governator.ProvisionMetrics.Visitor;

@Singleton
public class DIProvisionResource {
    private final ProvisionMetrics metrics;

    @Inject
    public DIProvisionResource(ProvisionMetrics metrics) {
        this.metrics = metrics;
    }
    
    public static interface Node {
        String getKey();
        int getIndent();
        long getTotalDuration();
        long getDuration();
    }
    
    public List<Node> get() {
        List<Node> result = new ArrayList<>();
        metrics.accept(new Visitor() {
            int level = 1;
            
            @Override
            public void visit(Element entry) {
                result.add(new Node() {
                    @Override
                    public String getKey() {
                        return entry.getKey().toString();
                    }

                    @Override
                    public int getIndent() {
                        return level;
                    }

                    @Override
                    public long getTotalDuration() {
                        return entry.getTotalDuration(TimeUnit.MILLISECONDS);
                    }

                    @Override
                    public long getDuration() {
                        return entry.getDuration(TimeUnit.MILLISECONDS);
                    }
                    
                });
                level++;
                entry.accept(this);
                level--;
            }
        });
        return result;
    }
}
