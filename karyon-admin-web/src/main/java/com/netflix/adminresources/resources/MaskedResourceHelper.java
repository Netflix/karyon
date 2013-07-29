package com.netflix.adminresources.resources;

import java.util.HashSet;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.netflix.config.DynamicPropertyFactory;

public class MaskedResourceHelper {
	@VisibleForTesting
	public static final String MASKED_PROPERTY_NAMES = "netflix.platform.admin.resources.masked.property.names";
	public static final String MASKED_PROPERTY_VALUE = "**** MASKED ****";
	
	private static final Splitter SPLITTER = Splitter.on(',')
		       .trimResults()
		       .omitEmptyStrings();
	
	public static Set<String> getMaskedResourceSet() {
		String maskedResourceNames = DynamicPropertyFactory.getInstance().getStringProperty(MASKED_PROPERTY_NAMES, "").get();		
		
		Iterable<String> maskedResourceNamesIter = SPLITTER.split(maskedResourceNames);		
		
		Set<String> maskedResourceSet = new HashSet<String>();
		for (String maskedResource : maskedResourceNamesIter) {
			maskedResourceSet.add(maskedResource);
		}
		
		// add the MASKED_PROPERTY_NAMES property, itself, for super-duper security-obscurity
		maskedResourceSet.add(MASKED_PROPERTY_NAMES);
		
		return maskedResourceSet;
	}
}
