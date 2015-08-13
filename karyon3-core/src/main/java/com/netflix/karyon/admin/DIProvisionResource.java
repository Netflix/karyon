package com.netflix.karyon.admin;

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
    
    public String get() {
        final StringBuilder sb = new StringBuilder();
        metrics.accept(new Visitor() {
            int level = 1;
            
            @Override
            public void visit(Element entry) {
                sb.append(String.format("%" + (level * 3 - 2) + "s%s%s : %d ms (%d ms)\n", 
                        "",
                        entry.getKey().getTypeLiteral().toString(), 
                        entry.getKey().getAnnotation() == null ? "" : " [" + entry.getKey().getAnnotation() + "]",
                        entry.getTotalDuration(TimeUnit.MILLISECONDS),
                        entry.getDuration(TimeUnit.MILLISECONDS)
                        ));
                level++;
                entry.accept(this);
                level--;
            }
        });
        return sb.toString();
    }
}
