package com.netflix.karyon.admin;

import java.lang.Thread.State;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

@Singleton
public class ThreadsAdminResource {
    static class Details {
        private Thread thread;

        public Details(Thread t) {
            this.thread = t;
        }
        
        public String getName() {
            return thread.getName();
        }
        
        public State getState() {
            return thread.getState();
        }
        
        public long getId() {
            return thread.getId();
        }
        
        public long getPriority() {
            return thread.getPriority();
        }
        
        public String getGroup( ){
            return thread.getThreadGroup().getName();
        }
        
        public List<String> getStackTrace() {
            return Arrays
                    .asList(thread.getStackTrace())
                    .stream().map(
                        (e) ->         e.getClassName() 
                               + "#" + e.getMethodName() 
                               + "(" + e.getFileName() + ":" + e.getLineNumber() + ")")
                   .collect(Collectors.toList());
        }
    }
    
    public List<Details> get() {
        return Thread
                .getAllStackTraces()
                .keySet()
                .stream()
                .map((t) -> new Details(t))
                .collect(Collectors.toList());
    }
}
