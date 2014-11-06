package netflix.adminresources.resources;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.netflix.config.DynamicPropertyFactory;

import java.util.HashSet;
import java.util.Set;

public class MaskedResourceHelper {
    @VisibleForTesting
    public static final String MASKED_PROPERTY_NAMES = "netflix.platform.admin.resources.masked.property.names";
    public static final String MASKED_ENV_NAMES = "netflix.platform.admin.resources.masked.env.names";
    public static final String MASKED_PROPERTY_VALUE = "**** MASKED ****";

    private static final Splitter SPLITTER = Splitter.on(',')
            .trimResults()
            .omitEmptyStrings();

    public static Set<String> getMaskedPropertiesSet() {
        String maskedResourceNames = DynamicPropertyFactory.getInstance().getStringProperty(MASKED_PROPERTY_NAMES, "").get();
        Set<String> maskedPropertiesSet = getMaskedResourceSet(maskedResourceNames);
        // add the MASKED_PROPERTY_NAMES property, itself, for super-duper security-obscurity
        maskedPropertiesSet.add(MASKED_PROPERTY_NAMES);
        return maskedPropertiesSet;
    }

    public static Set<String> getMaskedEnvSet() {
        String maskedResourceNames = DynamicPropertyFactory.getInstance().getStringProperty(MASKED_ENV_NAMES, "").get();
        return getMaskedResourceSet(maskedResourceNames);
    }


    private static Set<String> getMaskedResourceSet(String maskedResourceNames) {
        Iterable<String> maskedResourceNamesIter = SPLITTER.split(maskedResourceNames);

        Set<String> maskedResourceSet = new HashSet<String>();
        for (String maskedResource : maskedResourceNamesIter) {
            maskedResourceSet.add(maskedResource);
        }
        return maskedResourceSet;
    }


}
