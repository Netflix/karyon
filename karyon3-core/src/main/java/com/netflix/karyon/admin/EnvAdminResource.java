package com.netflix.karyon.admin;

import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class EnvAdminResource {
    public Map<String, String> get() throws Exception {
        return System.getenv();
    }
}
