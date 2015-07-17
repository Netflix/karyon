package com.netflix.karyon.rxnetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.netflix.archaius.Config;
import com.netflix.karyon.admin.Admin;
import com.netflix.karyon.admin.rest.ControllerRegistry;

@Singleton
public class HtmlAdminView implements AdminView {
    private final ControllerRegistry registry;
    private final ConcurrentMap<String, String> templates = new ConcurrentHashMap<>();
    private final Config config;
    
    @Inject
    public HtmlAdminView(@Admin ControllerRegistry registry, Config config) {
        this.registry = registry;
        this.config = config;
    }
    
    class RenderSession {
        private Map<String, List<String>> queryParameters;
        private final StrSubstitutor sub;
        private Stack<StrLookup<String>> lookup = new Stack<>();
        
        RenderSession(Map<String, List<String>> queryParameters) {
            this.queryParameters = queryParameters;
            
            lookup.push(new StrLookup<String>() {
                @Override
                public String lookup(String key) {
                    try {
                        String[] parts = key.split(":");
                        if (parts[0].equals("template")) {
                            return getTemplate(parts[1]);
                        }
                        else if (parts[0].equals("prop")) {
                            return config.getString(parts[1]);
                        }
                        else if (parts[0].equals("func")){
                            Method m = RenderSession.this.getClass().getMethod(parts[1]);
                            m.setAccessible(true);
                            return (String) m.invoke(RenderSession.this);
                        }
                    } 
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
            
            this.sub = new StrSubstitutor(new StrLookup<String>() {
                @Override
                public String lookup(String key) {
                    for (StrLookup<String> l : lookup) {
                        String value = l.lookup(key);
                        if (value != null) {
                            return value;
                        }
                    }
                    return null;
                }
            }, "${", "}", '$');

        }

        String render() throws IOException {
            return sub.replace(getTemplate("index"));
        }
        
        public String renderHeader() throws IOException {
            StringBuilder sb = new StringBuilder();
            String activeController = getFirstQueryParameter("controller");
            
            for (String name : registry.getNames()) {
                lookup.push(StrLookup.mapLookup(ImmutableMap.of("name", name)));
                if (name.equals(activeController)) {
                    sb.append(sub.replace(getTemplate("active_menu_item")));
                }
                else {
                    sb.append(sub.replace(getTemplate("menu_item")));
                }
                lookup.pop();
            }
            return sb.toString();
        }
    
        String getFirstQueryParameter(String name) {
            List<String> activeController = queryParameters.get("controller");
            if (activeController != null && !activeController.isEmpty()) {
                return activeController.get(0);
            }
            return null;
        }
    }
    
    @Override
    public String render(Map<String, List<String>> queryParameters) throws IOException {
        return new RenderSession(queryParameters).render();
    }
    
    private String getTemplate(String name) throws IOException {
        String template = templates.get(name);
        if (template != null) {
            return template;
        }
        
        InputStream is = this.getClass().getResourceAsStream("/admin/"+name+".html");
        try (final Reader reader = new InputStreamReader(is)) {
            template = CharStreams.toString(reader);
//            templates.putIfAbsent(name, template);
            return template;
        }
    }
}
