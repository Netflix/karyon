package org.apache.logging.log4j.core;

import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;

public class ArchaiusLog4jContextFactory extends Log4jContextFactory {
    public ArchaiusLog4jContextFactory() {
        super(new AsyncLoggerContextSelector());
    }
}
