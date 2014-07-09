package com.netflix.karyon.servlet.blocking;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class HttpSessionTest {

    private static final String SESSION_ID = "MYSESSID";

    @Test
    public void testLastAccessedTime() throws Exception {
        HttpSessionImpl session = new HttpSessionImpl(SESSION_ID, 1);
        long lastAccessedTime = session.getLastAccessedTime();
        session.getAttribute("Duh");
        long lastAccessedTimePostGet = session.getLastAccessedTime();
        Assert.assertNotSame("Last accessed time not updated after get attribute.", lastAccessedTime,
                             lastAccessedTimePostGet);

        session.setAttribute("Duh", "dih");
        long lastAccessedTimePostUpdate = session.getLastAccessedTime();
        Assert.assertNotSame("Last accessed time not updated after set attribute.", lastAccessedTimePostGet,
                             lastAccessedTimePostUpdate);

        session.removeAttribute("Duh");
        long lastAccessedTimePostRemove = session.getLastAccessedTime();
        Assert.assertNotSame("Last accessed time not updated after remove attribute.", lastAccessedTimePostUpdate,
                             lastAccessedTimePostRemove);
    }

    @Test
    public void testAttribute() throws Exception {
        HttpSessionImpl session = new HttpSessionImpl(SESSION_ID, 1);
        String name = "Name";
        String value = "value";
        session.setAttribute(name, value);

        Assert.assertNotNull("Attribute not found", session.getAttribute(name));
        Assert.assertEquals("Unexpected attribute value", value, session.getAttribute(name));

        session.removeAttribute(name);
        Assert.assertNull("Attribute not removed", session.getAttribute(name));
    }
}
