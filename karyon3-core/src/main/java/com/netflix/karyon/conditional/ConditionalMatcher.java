package com.netflix.karyon.conditional;

import java.lang.annotation.Annotation;

public interface ConditionalMatcher<T extends Annotation> {
    boolean evaluate(T conditional);
}
