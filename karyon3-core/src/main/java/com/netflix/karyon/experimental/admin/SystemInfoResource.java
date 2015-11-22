package com.netflix.karyon.experimental.admin;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import javax.inject.Singleton;

import com.netflix.karyon.admin.AdminService;

@Singleton
@AdminService(name="system-info", index="get")
final class SystemInfoResource {
    public OperatingSystemMXBean get() {
        return ManagementFactory.getOperatingSystemMXBean();
    }
}
