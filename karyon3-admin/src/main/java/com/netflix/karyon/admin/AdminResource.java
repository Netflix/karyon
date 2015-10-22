package com.netflix.karyon.admin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier for all admin resources.
 * 
 * Admin resources are meant to be very lightweight with no dependencies.
 * Resource endpoints are derived via naming convention from the method names
 * using REST semantics.
 * 
 * // Resource {name} derived from multi-binding string
 * class MyResource {
 *     // {name}/
 *     public list() { 
 *     }
 *     
 *     // {name}/{id}
 *     public get(String id) {
 *     }
 *     
 *     // {name}/{id}/child
 *     public listChild(String id) {
 *     }
 *     
 *     // {name}/{id}/child/{childId}
 *     public getChild(String id, String childId) {
 *     }
 * }
 * 
 * @see AbstractAdminModule
 * @author elandau
 *
 */
@Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminResource {

}
