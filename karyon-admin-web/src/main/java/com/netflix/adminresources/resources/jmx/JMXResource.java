/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.adminresources.resources.jmx;

import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.Map.Entry;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Resource to expose JMX via JSON
 *
 * @author elandau
 *
 */
@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
@Path("/webadmin/jmx")
public class JMXResource {
    private static final Logger LOG = LoggerFactory.getLogger(JMXResource.class);
    private static final String CURRENT_VALUE = "CurrentValue";

    private JmxService jmx;

    public JMXResource() {
        LOG.info("JMXResource created");
        jmx = JmxService.getInstance();
    }

    /**
     * Return JSON representing the entire tree of MBeans in DynaTree format.
     *
     * @param key
     * @param mode
     */
    @GET
    public Response getMBeans(
            @QueryParam("key") @DefaultValue("root") String key,
            @QueryParam("mode") @DefaultValue("") String mode,
            @QueryParam("jsonp") @DefaultValue("") String jsonp)
            throws Exception {

        LOG.info("key" + key);
        DynaTreeNode root = new DynaTreeNode();
        for (String domain : jmx.getDomainList()) {
            root.putChild(jmx.getDomainTree(domain).setTitle(domain)
                    .setMode("domain"));
        }

        StringWriter out = new StringWriter();
        if (jsonp.isEmpty()) {
            root.getChildrenJSONArray().write(out);
        } else {
            out.append(jsonp).append("(");
            root.getChildrenJSONArray().write(out);
            out.append(");");
        }

        return Response.ok(out.toString()).header("Pragma", "no-cache")
                .header("Cache-Control", "no-cache").header("Expires", "0")
                .build();
    }



    /**
     * Return all the attributes and operations for a single mbean
     *
     * @param key
     *            Exact object name of MBean in String form
     * @param jsonp
     */
    @GET
    @Path("{key}")
    public Response getMBean(@PathParam("key") String key,
                             @QueryParam("jsonp") @DefaultValue("") String jsonp)
            throws Exception {
        LOG.info("key: " + key);

        JSONObject json = new JSONObject();

        ObjectName name = new ObjectName(key);
        json.put("domain", name.getDomain());
        json.put("property", name.getKeyPropertyList());

        if (key.contains("*")) {
            JSONObject keys = new JSONObject();
            for (Entry<String, Map<String, String>> attrs : jmx
                    .getMBeanAttributesByRegex(key).entrySet()) {
                keys.put(attrs.getKey(), attrs.getValue());
            }
            json.put("attributes", keys);
            json.put("multikey", true);
        } else {
            json.put("attributes", jmx.getMBeanAttributes(key));
            json.put("multikey", false);

            MBeanOperationInfo[] operations = jmx.getMBeanOperations(key);
            JSONArray ar = new JSONArray();
            for (MBeanOperationInfo operation : operations) {
                JSONObject obj = new JSONObject();
                obj.put("name", operation.getName());
                obj.put("description", operation.getDescription());
                obj.put("returnType", operation.getReturnType());
                obj.put("impact", operation.getImpact());

                JSONArray params = new JSONArray();
                for (MBeanParameterInfo param : operation.getSignature()) {
                    JSONObject p = new JSONObject();
                    p.put("name", param.getName());
                    p.put("type", param.getType());
                    params.put(p);
                }
                obj.put("params", params);
                ar.put(obj);
            }
            json.put("operations", ar);
        }

        StringWriter out = new StringWriter();
        if (jsonp.isEmpty()) {
            json.write(out);
        } else {
            out.append(jsonp).append("(");
            json.write(out);
            out.append(");");
        }
        return Response.ok(out.toString()).type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Execute an operation on an mbean.
     *
     * @param formParams
     * @param key
     * @param jsonp
     * @param name
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{key}/{op}")
    public Response invokeMbeanOperation(
            MultivaluedMap<String, String> formParams,
            @PathParam("key") String key, @QueryParam("jsonp") String jsonp,
            @PathParam("op") String name) throws Exception {

        LOG.info("invoke " + key + " op=" + name);
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        Map<String, String> params = new TreeMap<String, String>();
        for (Entry<String, List<String>> entry : formParams.entrySet()) {
            if (entry.getKey().equals("op"))
                continue;
            if (entry.getValue().size() > 0)
                params.put(entry.getKey(), entry.getValue().get(0));
            else
                params.put(entry.getKey(), "");
        }

        ObjectName objName = new ObjectName(key);
        MBeanInfo info = mBeanServer.getMBeanInfo(objName);
        for (MBeanOperationInfo op : info.getOperations()) {
            if (op.getName().equals(name)) {
                List<String> signature = new ArrayList<String>();
                for (MBeanParameterInfo s : op.getSignature()) {
                    signature.add(s.getType());
                }
                Object result = mBeanServer.invoke(objName, name, params
                        .values().toArray(new String[params.size()]), signature
                        .toArray(new String[signature.size()]));
                JSONObject json = new JSONObject();
                json.put("key", key);
                json.put("operation", name);
                if (result != null) {
                    json.put("response", result.toString());
                }
                json.put("type", op.getReturnType());

                StringWriter out = new StringWriter();
                if (jsonp.isEmpty()) {
                    json.write(out);
                } else {
                    out.append(jsonp).append("(");
                    json.write(out);
                    out.append(");");
                }
                return Response.ok(out.toString())
                        .type(MediaType.APPLICATION_JSON).build();
            }
        }

        return Response.serverError().build();
    }

    /**
     * Return all the attributes and operations for a MBeans whose
     * {@link ObjectName}s match the passed in regex.
     *
     * @param objNameRegex
     *            regex for {@link ObjectName} of MBean(s). The
     *            {@link ObjectName} documentation explains what kinds of regex
     *            expressions are valid
     * @param jsonp
     *            if non-empty, Jsonp output is returned instead of Json
     */
    @GET
    @Path("/mbeans")
    public Response getMBeansByRegex(
            @QueryParam("objNameRegex") String objNameRegex,
            @QueryParam("jsonp") @DefaultValue("") String jsonp) {
        try {
            ObjectName objNameForRegex = new ObjectName(objNameRegex);
            MBeanServer mBeanServer = ManagementFactory
                    .getPlatformMBeanServer();
            Set<ObjectName> objs = mBeanServer
                    .queryNames(objNameForRegex, null);
            JSONObject result = new JSONObject();
            for (ObjectName objName : objs) {
                JSONObject json = new JSONObject();
                try {
                    json.put("attributes", emitAttributes(objName));
                } catch (Exception e) {
                    json.put("attributes", emitAttributes(objName));
                }
                json.put("operations", emitOperations(objName));
                result.put(objName.getCanonicalName(), json);
            }
            StringWriter out = new StringWriter();
            if (jsonp.isEmpty()) {
                result.write(out);
            } else {
                out.append(jsonp).append("(");
                result.write(out);
                out.append(");");
            }
            return Response.ok(out.toString()).type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            LOG.error(
                    "Error while retrieving mbeans for regex:" + objNameRegex,
                    e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }



    /**
     * Generate JSON for the MBean attributes
     *
     * @return
     * @throws Exception
     */
    private JSONObject emitAttributes(ObjectName objName) throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(objName);
        JSONObject resp = new JSONObject();
        if (mBeanInfo != null) {
            MBeanAttributeInfo[] attrs = mBeanInfo.getAttributes();
            if (attrs != null) {
                List<String> attrNames = new ArrayList<String>(attrs.length);
                for (MBeanAttributeInfo attr : attrs) {
                    attrNames.add(attr.getName());
                }
                AttributeList attrList = mBeanServer.getAttributes(objName,
                        attrNames.toArray(new String[0]));
                for (Attribute attr : attrList.asList()) {
                    Object value = attr.getValue();
                    String attrName = attr.getName();
                    if (attrName != null && value != null) {
                        String attrValue = null;

                        if (value instanceof CompositeDataSupport) {
                            CompositeDataSupport compositeValue = (CompositeDataSupport) value;
                            if (compositeValue != null) {
                                try {
                                    if (compositeValue.containsKey(CURRENT_VALUE)) {
                                        Object curValue = compositeValue
                                                .get(CURRENT_VALUE);
                                        attrValue = (curValue == null ? "null"
                                                : curValue.toString());
                                    }
                                }
                                catch (Exception e) {
                                    attrValue = compositeValue.toString();
                                }
                            }
                        }

                        if (attrValue == null) {
                            attrValue = value.toString();
                        }
                        resp.put(attrName, (attrValue == null ? "null"
                                : attrValue));
                    }
                }
            }
        }
        return resp;
    }

    /**
     * Generate JSON for the MBean operations
     *
     * @param objName
     * @return
     * @throws Exception
     */
    private JSONArray emitOperations(ObjectName objName) throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(objName);
        JSONArray ar = new JSONArray();

        MBeanOperationInfo[] operations = mBeanInfo.getOperations();
        for (MBeanOperationInfo operation : operations) {
            JSONObject obj = new JSONObject();
            obj.put("name", operation.getName());
            obj.put("description", operation.getDescription());
            obj.put("returnType", operation.getReturnType());
            obj.put("impact", operation.getImpact());

            JSONArray params = new JSONArray();
            for (MBeanParameterInfo param : operation.getSignature()) {
                JSONObject p = new JSONObject();
                p.put("name", param.getName());
                p.put("type", param.getType());
                params.put(p);
            }
            obj.put("params", params);
            ar.put(obj);
        }
        return ar;
    }

}

