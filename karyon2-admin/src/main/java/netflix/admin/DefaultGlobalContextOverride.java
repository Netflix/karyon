package netflix.admin;

import java.util.Properties;

public class DefaultGlobalContextOverride implements GlobalModelContextOverride {
    @Override
    public Properties overrideProperties(Properties properties) {
        return properties;
    }
}
