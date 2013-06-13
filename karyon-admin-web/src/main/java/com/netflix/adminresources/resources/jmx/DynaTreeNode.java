package com.netflix.adminresources.resources.jmx;

import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Represents a single node in a tree and simplifies adding children and serializing the final
 * tree into JSON.
 * @author elandau
 */
public class DynaTreeNode {
	private String title;
	private String key;
	private String mode;
	private Map<String, DynaTreeNode> children;
	private boolean noLink = true;
	
	public DynaTreeNode() {
		
	}
	
	public DynaTreeNode setTitle(String title) {
		this.title = title;
		return this;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public DynaTreeNode setNoLink(boolean noLink) {
		this.noLink = noLink;
		return this;
	}
	
	public boolean getNoLink() {
		return this.noLink;
	}
	
	public DynaTreeNode setKey(String key) {
		this.key = key;
		return this;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public DynaTreeNode setMode(String mode) {
		this.mode = mode;
		return this;
	}

	public String getMode() {
		return this.mode;
	}
	
	public Map<String, DynaTreeNode> getChildren() {
		if (children == null) {
			children = new TreeMap<String, DynaTreeNode>();
		}
		return children;
	}
	
	public DynaTreeNode getChild(String title) {
		return getChildren().get(title);
	}
	
	public void putChild(DynaTreeNode child) {
		getChildren().put(child.title, child);
	}
	
	@SuppressWarnings("unchecked")
    public JSONObject toJSONObject() throws JSONException {
		return new JSONObject()
	    	.put("title", title)
	    	.put("key",   key)
	    	.put("noLink", noLink)
	    	.put("mode",  mode)
	    	.put("expand", true)
			.put("children", getChildrenJSONArray());
	}

	@SuppressWarnings("unchecked")
    public JSONArray getChildrenJSONArray() {
		JSONArray ar = null;
		if (children != null) {
			ar = new JSONArray();
			for (DynaTreeNode a : children.values()) {
				try {
	                ar.put(a.toJSONObject());
                } catch (JSONException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
			}
		}
		return ar;
	}
}
