package com.netflix.adminresources.pages;

import com.netflix.adminresources.AbstractAdminPageInfo;
import com.netflix.adminresources.AdminPage;

@AdminPage
public class JmxPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "jmx";
    public static final String NAME = "JMX";

    public JmxPage() {
        super(PAGE_ID, NAME);
    }
}
