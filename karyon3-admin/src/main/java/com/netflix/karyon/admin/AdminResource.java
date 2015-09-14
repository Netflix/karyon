package com.netflix.karyon.admin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Qualifier for all admin resources.
 * 
 * Admin resources and meant to be very lightweight with no dependencies.
 * Resource endpoints are derived via naming convention from the method names
 * using REST semantics.
 *
 * // Resource {name} derived from multi-binding string
 * public class MyResource {
 *     // {name}/
 *     public List&lt;Object&gt; get() {
 *     }
 *     
 *     // {name}/{id}
 *     public Object get(String id) {
 *     }
 *     
 *     // {name}/{id}/child
 *     public List&lt;Object&gt; listChild(String id) {
 *     }
 *     
 *     // {name}/{id}/child/{childId}
 *     public Object getChild(String id, String childId) {
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
