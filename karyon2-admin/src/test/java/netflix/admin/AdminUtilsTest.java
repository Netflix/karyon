package netflix.admin;


import com.netflix.explorers.PropertiesGlobalModelContext;
import org.junit.Test;

import java.util.Properties;

import static junit.framework.Assert.assertTrue;

public class AdminUtilsTest {

    @Test
    public void environmentPropertyLoaded() {
        final Properties properties = AdminUtils.loadAdminConsoleProps();
        assertTrue(properties.getProperty(PropertiesGlobalModelContext.PROPERTY_ENVIRONMENT_NAME) != null);
    }

    @Test
    public void regionPropertyLoaded() {
        final Properties properties = AdminUtils.loadAdminConsoleProps();
        assertTrue(properties.getProperty(PropertiesGlobalModelContext.PROPERTY_CURRENT_REGION) != null);
    }


}
