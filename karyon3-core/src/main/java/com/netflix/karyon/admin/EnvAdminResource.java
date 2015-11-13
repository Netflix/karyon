package com.netflix.karyon.admin;

import javax.inject.Singleton;

import java.util.Map;

@Singleton
@AdminService(name="env", index="list")
public class EnvAdminResource {
    public Map<String, String> list() throws Exception {
        return System.getenv();
    }
}
