package com.netflix.karyon;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Module;
import com.netflix.karyon.conditional.Conditional;

/**
 * ClassPath scanner using Guava's ClassPath
 * 
 * @author elandau
 */
public class ClassPathConditionalModuleListProvider extends ClassPathModuleListProvider {

    public ClassPathConditionalModuleListProvider(String... packages) {
        super(Arrays.asList(packages));
    }
    
    public ClassPathConditionalModuleListProvider(List<String> packages) {
        super(packages);
    }
    
    @Override
    protected boolean isAllowed(Class<? extends Module> cls) {
        for (Annotation annot : cls.getAnnotations()) {
            if (null != annot.annotationType().getAnnotation(Conditional.class)) {
                return true;
            }
        }
        return false;
    }
}
