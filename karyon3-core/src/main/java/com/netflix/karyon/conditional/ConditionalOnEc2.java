package com.netflix.karyon.conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.netflix.governator.auto.annotations.Conditional;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnEc2Condition.class)
public @interface ConditionalOnEc2 {
}
