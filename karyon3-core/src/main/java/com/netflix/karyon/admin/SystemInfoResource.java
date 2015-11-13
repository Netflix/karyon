package com.netflix.karyon.admin;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import javax.inject.Singleton;

@Singleton
@AdminService(name="system-info", index="get")
public class SystemInfoResource {
    public OperatingSystemMXBean get() {
        return ManagementFactory.getOperatingSystemMXBean();
    }
}
