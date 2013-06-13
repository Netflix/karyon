package com.netflix.adminresources.resources.jmx;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxService {
    private static final Logger LOG = LoggerFactory.getLogger(JmxService.class);

    private static final String CSV_PATTERN   = "\"([^\"]+?)\",?|([^,]+),?|,";
    private static final String CURRENT_VALUE = "CurrentValue";
    private static final String MODE_DOMAIN   = "domain";
    private static final String MODE_INNER    = "inner";
    private static final String MODE_LEAF     = "leaf";
    
    private static class Holder {
        private static final JmxService instance = new JmxService();
    }
    
    private final MBeanServer mBeanServer;
    
    private static Pattern csvRE = Pattern.compile(CSV_PATTERN);

    private JmxService() {
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    public static JmxService getInstance() {
        return Holder.instance;
    }
    
    /**
     * Return the list of all domains on this server
     * @return
     */
    public List<String> getDomainList() {
        return Lists.newArrayList(mBeanServer.getDomains());
    }
    
    /**
     * Return subtree of nodes for a domain
     */
    public DynaTreeNode getDomainTree(String domainName) {
        DynaTreeNode domainNode = new DynaTreeNode()
                .setTitle(domainName)
                .setKey(domainName)
                .setMode(MODE_DOMAIN);
        try {
            // List all objects in the domain
            ObjectName name = new ObjectName(domainName + ":*");
            Set<ObjectName> objs = mBeanServer.queryNames(name, null);
            
            // Convert object naems to a tree
            for (ObjectName objName : objs) {
                MBeanInfo info = mBeanServer.getMBeanInfo(objName);
                Matcher m = csvRE.matcher(objName.getKeyPropertyListString());
                
                DynaTreeNode node = domainNode;
                StringBuilder innerKey = new StringBuilder();
                innerKey.append(domainName).append(":");
                
                while (m.find()) {
                    String title = StringUtils.removeEnd(m.group(), ",");
                    String key   = StringUtils.substringBefore(title, "=");
                    String value = StringUtils.substringAfter(title, "=");
                    value = StringUtils.removeStart(value, "\"");
                    value = StringUtils.removeEnd  (value, "\"");
                    
                    innerKey.append(title).append(",");
                    
                    DynaTreeNode next = node.getChild(value);
                    if (next == null) {
                        next = new DynaTreeNode()
                            .setTitle(value)
                            .setMode(MODE_INNER)
                            .setKey(innerKey.toString() + "*")
                            .setNoLink(false);
                        node.putChild(next);
                    }
                    node = next;
                }
                
                node.setKey(objName.getCanonicalName())
                    .setMode(MODE_LEAF);

                if (   info.getAttributes() != null
                    || info.getOperations() != null
                    || info.getNotifications() != null) {
                    node.setNoLink(false);
                }  
            }
        } catch (MalformedObjectNameException e) {
            LOG.error("Exception in getDomainTree ", e);
        } catch (IntrospectionException e) {
            LOG.error("Exception in getDomainTree ", e);
        } catch (ReflectionException e) {
            LOG.error("Exception in getDomainTree ", e);
        } catch (InstanceNotFoundException e) {
            LOG.error("Exception in getDomainTree ", e);
        } catch (RuntimeException e) {
            LOG.error("Exception in getDomainTree ", e);
        }
        return domainNode;
    }
    
    /**
     * Return all keysace in a domain
     * @param domainName
     * @return
     */
    public List<String> getDomainKeys(String domainName) {
        return getKeysFromRegex(domainName + ":*");
    }
    
    /**
     * Return the list of all keys matching a regex
     * @param regex
     * @return
     */
    public List<String> getKeysFromRegex(String regex) {
        List<String> keys = Lists.newArrayList();
        
        try {
            // List all objects in the domain
            ObjectName name = new ObjectName(regex);
            Set<ObjectName> objs = mBeanServer.queryNames(name, null);
            
            // Convert object naems to a tree
            for (ObjectName objName : objs) {
                MBeanInfo info = mBeanServer.getMBeanInfo(objName);
                keys.add(objName.getCanonicalName());
            }
        } catch (Exception e) {
            LOG.error("Exception in getKeysFromRegex ", e);
        }
        
        return keys;
    }
    
    /**
     * Return a map of all attributes for objects matching the regex.  
     * @param regex
     * @return
     * @throws Exception
     */
    public Map<String, Map<String, String>> getMBeanAttributesByRegex(String regex) throws Exception {
        Map<String, Map<String, String>> result = Maps.newLinkedHashMap();
        ObjectName name = new ObjectName(regex);
        Set<ObjectName> objs = mBeanServer.queryNames(name, null);
        
        // Convert object naems to a tree
        for (ObjectName objName : objs) {
            result.put(objName.getCanonicalName(), getMBeanAttributes(objName));
        }

        return result;
    }
    
    /**
     * Get list of all attributes of the specified key
     * @param key
     * @return
     */
    public Map<String, String> getMBeanAttributes(String key) throws Exception {
        return getMBeanAttributes(new ObjectName(key));
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
                AttributeList attrList = mBeanServer.getAttributes(objName, attrNames.toArray(new String[0]));
                
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
                            if (compositeValue != null) {
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
                        } 
                        if (attrValue == null) {
                            attrValue = value.toString();
                        }
                    }
                    else {
                        value = "none";
                    }
                    
                    response.put(attrName, attrValue);
                }
            }
        }
        return response;
    }
    
    /**
     * Return all operations for the specified mbean name
     * @param name
     * @return
     * @throws Exception
     */
    public MBeanOperationInfo[] getMBeanOperations(String name) throws Exception {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(new ObjectName(name));
        return mBeanInfo.getOperations();
    }
    
}
