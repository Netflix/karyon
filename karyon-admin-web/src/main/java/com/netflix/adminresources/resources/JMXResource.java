package com.netflix.adminresources.resources;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nitesh Kant
 * @author elandau
 */
@Beta
@Path("/admin/jmx")
@Produces(MediaType.APPLICATION_JSON)
public class JMXResource {

    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private static final String CURRENT_VALUE = "CurrentValue";

    @GET
    public Response getJmxBeans() {
        List<MbeanData> mbeans = new ArrayList<MbeanData>();
        String[] domains = mBeanServer.getDomains();
        for (String domain : domains) {
            ObjectName name = null;
            try {
                name = new ObjectName(domain + ":*");
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
            Set<ObjectName> objs = mBeanServer.queryNames(name, null);
            for (ObjectName obj : objs) {
                MbeanData data = new MbeanData();
                data.displayName = obj.getCanonicalName();
                try {
                    data.attributes = getMBeanAttributes(obj);
                    mbeans.add(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
        Gson gson = gsonBuilder.create();
        String propsJson = gson.toJson(mbeans);
        return Response.ok(propsJson).build();
    }

    /**
     * Get list of all attributes of an object
     * @param objName
     * @return
     * @throws Exception
     */
    private Map<String, String> getMBeanAttributes(ObjectName objName) throws Exception  {
        Map<String, String> response = Maps.newLinkedHashMap();
        // Look for the object
        MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(objName);
        if (mBeanInfo != null) {

            // Does it have attributes?
            MBeanAttributeInfo[] attrs = mBeanInfo.getAttributes();
            if (attrs != null) {
                // List all attributes
                List<String> attrNames = Lists.newArrayList();
                for (MBeanAttributeInfo attr : attrs) {
                    attrNames.add(attr.getName());
                }
                AttributeList attrList =
                        mBeanServer.getAttributes(objName, attrNames.toArray(new String[attrNames.size()]));

                // Process each attribute
                for (Attribute attr : attrList.asList()) {
                    String attrName = attr.getName();
                    Object value    = attr.getValue();
                    String attrValue = null;

                    // Attribute has data
                    if (value != null) {
                        // Special case of CompositeDataSuppert
                        if (value instanceof CompositeDataSupport) {
                            CompositeDataSupport compositeValue = (CompositeDataSupport) value;
                            try {
                                if (compositeValue.containsKey(CURRENT_VALUE)) {
                                    Object curValue = compositeValue.get(CURRENT_VALUE);
                                    attrValue = (curValue == null ? "null" : curValue.toString());
                                }
                            }
                            catch (Exception e) {
                                attrValue = compositeValue.toString();
                            }
                        }
                        if (attrValue == null) {
                            attrValue = value.toString();
                        }
                    }
                    else {
                        attrValue = "none";
                    }

                    response.put(attrName, attrValue);
                }
            }
        }
        return response;
    }

    private static class MbeanData {

        private String displayName;
        private Map<String, String> attributes;

        @SuppressWarnings("unused")
        public String getDisplayName() {
            return displayName;
        }

        @SuppressWarnings("unused")
        public Map<String, String> getAttributes() {
            return attributes;
        }
    }
}
