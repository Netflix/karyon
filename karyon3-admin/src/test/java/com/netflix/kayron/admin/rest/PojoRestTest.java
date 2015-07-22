package com.netflix.kayron.admin.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.netflix.karyon.admin.rest.DefaultResourceContainer;

public class PojoRestTest {
    public static class TestResource {
        public List<String> get() {
            return Arrays.asList("a", "b");
        }
        
        public String get(String name) {
            return "found";
        }
        
        public List<String> getFoo(String name) {
            return Arrays.asList("foo_a", "foo_b");
        }
        
        public String getFoo(String name, String foo) {
            return name + "_" + foo + "_found";
        }
        
        public List<String> getFooBar(String name, String foo) {
            return Arrays.asList(name + "_" + foo + "_a", name + "_" + foo + "foo_b");
        }
        
        // root/:name/foo/:foo/bar/:bar
        public String getFooBar(String name, String foo, String bar) {
            return name + "_" + foo + "_" + bar + "_found";
        }
    }
    
    @Test
    public void test() throws Exception {
        Map<String, Object> controllers = new HashMap<>();
        controllers.put("test", new TestResource());
        
        DefaultResourceContainer rest = new DefaultResourceContainer(controllers);
        rest.invoke("test", Collections.<String>emptyList());
        System.out.println(rest.invoke("test", Arrays.asList("1")));
        System.out.println(rest.invoke("test", Arrays.asList("1", "foo")));
        System.out.println(rest.invoke("test", Arrays.asList("1", "foo", "2")));
        System.out.println(rest.invoke("test", Arrays.asList("1", "foo", "2", "bar")));
        System.out.println(rest.invoke("test", Arrays.asList("1", "foo", "2", "bar", "3")));
    }
}
