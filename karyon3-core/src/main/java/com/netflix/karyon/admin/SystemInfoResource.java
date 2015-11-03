package com.netflix.karyon.admin;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class SystemInfoResource {
    public OperatingSystemMXBean get() {
        return ManagementFactory.getOperatingSystemMXBean();
    }
}
