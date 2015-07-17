package com.netflix.karyon.admin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier for all Admin related bindings.  For example, Controllers
 * for admin must be annotated with @Admin
 * 
 * @see AbstractAdminModule
 * @author elandau
 *
 */
@Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Admin {

}
