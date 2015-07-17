package com.netflix.kayron.admin.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.netflix.karyon.admin.rest.Controller;
import com.netflix.karyon.admin.rest.DefaultControllerRegistry;

public class PojoRestTest {
    public static class TestController implements Controller {
        public List<String> list() {
            return Arrays.asList("a", "b");
        }
        
        public String find(String name) {
            return "found";
        }
        
        public List<String> listFoo(String name) {
            return Arrays.asList("foo_a", "foo_b");
        }
        
        public String findFoo(String name, String foo) {
            return name + "_" + foo + "_found";
        }
        
        public List<String> listFooBar(String name, String foo) {
            return Arrays.asList(name + "_" + foo + "_a", name + "_" + foo + "foo_b");
        }
        
        // root/:name/foo/:foo/bar/:bar
        public String findFooBar(String name, String foo, String bar) {
            return name + "_" + foo + "_" + bar + "_found";
        }
    }
    
    @Test
    public void test() throws Exception {
        Map<String, Controller> controllers = new HashMap<>();
        controllers.put("test", new TestController());
        
        DefaultControllerRegistry rest = new DefaultControllerRegistry(controllers);
        rest.invoke("test", Collections.<String>emptyList());
        System.out.println(rest.invoke("test", Arrays.asList("1")));
        System.out.println(rest.invoke("test", Arrays.asList("1", "foo")));
        System.out.println(rest.invoke("test", Arrays.asList("1", "foo", "2")));
        System.out.println(rest.invoke("test", Arrays.asList("1", "foo", "2", "bar")));
        System.out.println(rest.invoke("test", Arrays.asList("1", "foo", "2", "bar", "3")));
    }
}
