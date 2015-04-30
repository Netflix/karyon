package netflix.admin;

import java.util.Properties;

public interface GlobalModelContextOverride {
    Properties overrideProperties(Properties properties);
}
