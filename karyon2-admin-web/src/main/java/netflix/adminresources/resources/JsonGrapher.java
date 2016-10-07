package netflix.adminresources.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.grapher.BindingEdge;
import com.google.inject.grapher.DependencyEdge;
import com.google.inject.grapher.ImplementationNode;
import com.google.inject.grapher.InstanceNode;
import com.google.inject.grapher.InterfaceNode;
import com.google.inject.grapher.KaryonAbstractInjectorGrapher;
import com.google.inject.grapher.NameFactory;
import com.google.inject.grapher.Node;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class JsonGrapher extends KaryonAbstractInjectorGrapher {
    private final Map<Key<?>, GraphNode> nodes = Maps.newHashMap();
    private final NameFactory nameFactory;
    private PrintWriter out;

    private enum Type {
        Instance,
        Implementation,
        Interface
    }
    
    @SuppressWarnings("unused")
    private class GraphNode {
        private final List<GraphNode> boundTo = new ArrayList<>();
        private final Node node;
        private final Type type;
        private final List<GraphNode> dependencies = new ArrayList<>();
        private long duration;
        private long order = -1;
        
        public GraphNode(Node node, Type type) {
            this.node = node;
            this.type = type;
        }
        
        public void boundTo(GraphNode id) {
            if (!getName().equals(id.getName())) {
                this.boundTo.add(id);
            }
        }
        
        public void dependsOn(GraphNode id) {
            this.dependencies.add(id);
        }
        
        public String getAnnotation() {
            return nameFactory.getAnnotationName(node.getId().getKey());
        }
        
        public Type getType() {
            return type;
        }
        
        public String getSource() {
            return nameFactory.getSourceName(node.getSource());
        }
        
        public String getName() {
            String annot = nameFactory.getAnnotationName(node.getId().getKey());
            return annot.isEmpty() 
                ? nameFactory.getClassName(node.getId().getKey())
                : nameFactory.getClassName(node.getId().getKey()) + ":" + annot;
        }
        
        public List<String> getDependencies() {
            List<String> dep = new ArrayList<>();
            for (GraphNode node : dependencies) {
                if (node != null) {
                    dep.add(node.getName());
                }
            }
            return dep;
        }
        
        public List<String> getBoundTo() {
            List<String> dep = new ArrayList<>();
            for (GraphNode node : boundTo) {
                dep.add(node.getName());
            }
            return dep;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
        
        public long getDuration() {
            return duration;
        }
        
        public void setOrder(int order) {
            this.order = order;
        }
        
        public long getOrder() {
            return this.order;
        }
    }
    
    @Inject
    JsonGrapher(NameFactory nameFactory) {
        this.nameFactory = nameFactory;
    }

    @Override
    protected void reset() {
        nodes.clear();
    }

    @Override
    protected void postProcess() throws IOException {
        ObjectMapper m = new ObjectMapper();
        m.writer()
         .with(SerializationFeature.INDENT_OUTPUT)
         .writeValue(out, nodes.values());
    }
    
    public String quotes(String str) {
        return "\"" + str + "\"";
    }

    @Override
    protected void newInterfaceNode(InterfaceNode node) {
        nodes.put(node.getId().getKey(), new GraphNode(node, Type.Interface));
    }

    @Override
    protected void newImplementationNode(ImplementationNode node) {
        nodes.put(node.getId().getKey(), new GraphNode(node, Type.Implementation));
    }

    @Override
    protected void newInstanceNode(InstanceNode node) {
        nodes.put(node.getId().getKey(), new GraphNode(node, Type.Instance));
    }

    @Override
    protected void newDependencyEdge(DependencyEdge edge) {
        nodes.get(edge.getFromId().getKey())
             .dependsOn(nodes.get(edge.getToId().getKey()));
    }

    @Override
    protected void newBindingEdge(BindingEdge edge) {
        nodes.get(edge.getFromId().getKey())
             .boundTo(nodes.get(edge.getToId().getKey()));
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

}