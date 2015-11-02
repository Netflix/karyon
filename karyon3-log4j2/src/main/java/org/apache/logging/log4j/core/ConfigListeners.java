package org.apache.logging.log4j.core;

import java.util.function.Consumer;

import com.netflix.archaius.Config;
import com.netflix.archaius.ConfigListener;

/**
 * Utility class to simplify creating ConfigListener.
 * 
 * TODO: Move this archaius
 */
final class ConfigListeners {
    static ConfigListener any(Consumer<Config> consumer) {
        return new ConfigListener() {
            @Override
            public void onConfigAdded(Config config) {
                consumer.accept(config);
            }

            @Override
            public void onConfigRemoved(Config config) {
                onConfigAdded(config);
            }

            @Override
            public void onConfigUpdated(Config config) {
                onConfigAdded(config);
            }

            @Override
            public void onError(Throwable error, Config config) {
                if (config != null)
                    onConfigAdded(config);
            }
        };
    }
}
