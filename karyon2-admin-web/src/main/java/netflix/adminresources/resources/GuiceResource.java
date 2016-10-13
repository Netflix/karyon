package netflix.adminresources.resources;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.netflix.governator.ProvisionMetrics;
import com.netflix.governator.ProvisionMetrics.Element;
import com.netflix.governator.ProvisionMetrics.Visitor;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/guice")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public final class GuiceResource {
    @Inject
    Injector injector;
    
    @Inject
    JsonGrapher grapher;
    
    @Inject
    ProvisionMetrics metrics;
    
    @GET
    @Path("keys")
    public String get() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos);
        
        grapher.setOut(out);
        grapher.graph(injector.getParent());
        return baos.toString("UTF-8");
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
    
    @GET
    @Path("metrics")
    public String getProvision() throws Exception {
        return new GsonBuilder()
            .serializeNulls()
            .create()
            .toJson(list());
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
