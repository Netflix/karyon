package netflix.admin;

import com.google.inject.ImplementedBy;

import java.util.Properties;

@ImplementedBy(DefaultGlobalContextOverride.class)
public interface GlobalModelContextOverride {
    Properties overrideProperties(Properties properties);
}
