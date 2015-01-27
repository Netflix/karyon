package netflix.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AdminUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AdminExplorerManager.class);

    public static Properties loadAdminConsoleProps() {
        final Properties properties = new Properties();
        final InputStream propsResourceStream = AdminUtils.class.getClassLoader().getResourceAsStream("admin-explorers.properties");
        if (propsResourceStream != null) {
            try {
                properties.load(propsResourceStream);
            } catch (IOException e) {
                LOG.error("Exception loading admin console properties file.", e);
            }
        }

        return properties;
    }

}
