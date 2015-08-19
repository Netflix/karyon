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
@Conditional(OnLocalDevCondition.class)
public @interface ConditionalOnLocalDev {
}
