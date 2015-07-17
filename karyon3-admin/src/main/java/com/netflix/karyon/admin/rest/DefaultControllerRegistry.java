package com.netflix.karyon.admin.rest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DefaultControllerRegistry implements ControllerRegistry {
    private static final String CAMEL_CASE_PATTERN = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"; // 3rd edit, getting better
    
    private static class Node {
        Map<String, Node> children;
        Invoker listInvoker;
        Invoker findInvoker;
        
        Node getChild(String name) {
            if (children == null) {
                return null;
            }
            return children.get(name);
        }

        Node getOrCreateChild(String name) {
            if (children == null) {
                children = new HashMap<>();
            }
            Node node = children.get(name);
            if (node == null) {
                node = new Node();
                children.put(name, node);
            }
            return node;
        }

        void setListInvoker(Invoker invoker) {
            this.listInvoker = invoker;
        }

        void setFindInvoker(Invoker invoker) {
            this.findInvoker = invoker;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Node[");
            boolean first = true;
            sb.append("actions=[");
            if (this.listInvoker != null) {
                sb.append("list ");
            }
            if (this.findInvoker != null) {
                sb.append("find ");
            }
            sb.append("]");
            sb.append(" children=[");
            if (children != null) {
                for (Map.Entry<String, Node> entry : children.entrySet()) {
                    if (!first) {
                        sb.append(", ");
                    }
                    first = false;
                    sb.append(entry.getKey() + ": " + entry.getValue());
                }
            }
            sb.append("]");
            sb.append("]");
            return sb.toString();
        }
    }
    
    private final Map<String, Node> controllers = new HashMap<>();
    
    public DefaultControllerRegistry(Map<String, Controller> controllers) throws Exception {
        for (final Entry<String, Controller> controller : controllers.entrySet()) {
            Node root = new Node();
            this.controllers.put(controller.getKey(), root);
            
            for (final Method method : controller.getValue().getClass().getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                
                // Determine verb
                String[] m = method.getName().split(CAMEL_CASE_PATTERN);
                Iterator<String> iter = Arrays.asList(m).iterator();
                String action = iter.next();
                
                // Determine nesting of resources
                Node current = root;
                while (iter.hasNext()) {
                    current = current.getOrCreateChild(iter.next().toLowerCase());
                }
                
                // Determine binders for each argument
                // TODO: Bind to DI
                // TODO: Bind to Optional
                final List<IndexedStringConverter> converters = new ArrayList<>();
                int argIndex = 0;
                for (Class<?> type : method.getParameterTypes()) {
                    try {
                        converters.add(
                            new IndexedStringConverter(argIndex, 
                                new ConstructorStringConverter(type.getConstructor(String.class))));
                    }
                    catch (NoSuchMethodException e) {   
                        try {
                            converters.add(
                                new IndexedStringConverter(argIndex, 
                                    new ValueOfStringConverter(type.getDeclaredMethod("valueOf", String.class))));
                        } catch (Exception e1) {
                            throw new Exception("TODO");
                        }
                    }
                    argIndex++;
                }
                
                Invoker invoker = new Invoker() {
                    @Override
                    public Object invoke(List<String> args) throws Exception {
                        Object[] params = new Object[converters.size()];
                        for (int i = 0; i < converters.size(); i++) {
                            params[i] = converters.get(i).convert(args);
                        }
                        
                        return method.invoke(controller.getValue(), params);
                    }
                };
                
                if ("find".equals(action) || "get".equals(action) || "show".equals(action)) {
                    current.setFindInvoker(invoker);
                }
                else if ("list".equals(action)) {
                    current.setListInvoker(invoker);
                }
                else {
                    throw new Exception(String.format("Invalid method action '%s'.  Supported actions: [list, find]", action));
                }
            }
            // TODO: Throw exception if no methods
        }
    }
    
    @Override
    public Object invoke(String controller, List<String> parts) throws Exception {
        List<String> args = new ArrayList<>();
        Node current = controllers.get(controller);
        if (current != null) {
            Iterator<String> iter = parts.iterator();
            while (iter.hasNext()) {
                String id = iter.next().toLowerCase();
                args.add(id);
                if (iter.hasNext()) {
                    String sub = iter.next().toLowerCase();
                    current = current.getChild(sub);
                    if (current == null) {
                        throw new Exception(String.format("Sub resource '%s' does not exist", sub));
                    }
                }
                else {
                    if (current.findInvoker == null) {
                        throw new Exception("Invoker not found for " + parts);
                    }
                    return current.findInvoker.invoke(args);
                }
            }
            
            if (current.listInvoker == null) {
                throw new Exception("Invoker not found for " + parts);
            }
            return current.listInvoker.invoke(args);
        }
        throw new Exception("Controller not found for " + controller);
    }
    
    @Override
    public Set<String> getNames() {
        return controllers.keySet();
    }
    
    @Override
    public List<String> getActions(String name) {
        return null;
    }
}
