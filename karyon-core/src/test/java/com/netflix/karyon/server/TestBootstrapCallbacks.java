package com.netflix.karyon.server;

import com.google.inject.Injector;
import com.netflix.karyon.util.KaryonTestSetupUtil;
import com.test.Bootstrap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * @author Nitesh Kant
 */
public class TestBootstrapCallbacks {

    private KaryonServer server;
    private Bootstrap bootstrap;

    @Before
    public void setUp() throws Exception {
        KaryonTestSetupUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        KaryonTestSetupUtil.tearDown(server);
    }

    @Test
    public void testCallbacks() throws Exception {
        startServer();
        Set<String> callbacksInvoked = bootstrap.getCallbacksInvoked();
        assertCallbackInvoked(callbacksInvoked, Bootstrap.BEFORE_INJECTION_CREATION);
        assertCallbackInvoked(callbacksInvoked, Bootstrap.CONFIGURE_BINDER);
        assertCallbackInvoked(callbacksInvoked, Bootstrap.CONFIGURE_BOOT_BINDER);
        assertCallbackInvoked(callbacksInvoked, Bootstrap.CREATE_INJECTOR);
        assertCallbackInvoked(callbacksInvoked, Bootstrap.GET_BASE_PKGS);
        assertCallbackInvoked(callbacksInvoked, Bootstrap.GET_BOOT_MODULE);
        assertCallbackInvoked(callbacksInvoked, Bootstrap.GET_CONFIG_PROVIDER);
        assertCallbackInvoked(callbacksInvoked, Bootstrap.GET_SERVICE_REG_CLIENT);
        assertCallbackInvoked(callbacksInvoked, Bootstrap.NEW_LIFECYCLE_INJ_BUILDER);
    }

    private static void assertCallbackInvoked(Set<String> callbacksInvoked, String callbackname) {
        Assert.assertTrue("Bootstrap callback: " + callbackname + " not invoked by karyon.", callbacksInvoked.contains(
                callbackname));
    }

    private Injector startServer() throws Exception {
        bootstrap = new Bootstrap();
        server = new KaryonServer(bootstrap);
        return KaryonTestSetupUtil.startServer(server);
    }
}
