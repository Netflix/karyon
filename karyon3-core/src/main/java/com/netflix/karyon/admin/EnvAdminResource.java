package com.netflix.karyon.admin;

import javax.inject.Singleton;

import com.netflix.karyon.admin.AdminService;

import java.util.Map;

@Singleton
@AdminService(name="env", index="list")
final class EnvAdminResource {
    public Map<String, String> list() throws Exception {
        return System.getenv();
    }
}
