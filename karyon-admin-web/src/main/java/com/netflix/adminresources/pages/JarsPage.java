package com.netflix.adminresources.pages;

import com.netflix.adminresources.AbstractAdminPageInfo;
import com.netflix.adminresources.AdminPage;

@AdminPage
public class JarsPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "jars";
    public static final String NAME = "Jars";

    public JarsPage() {
        super(PAGE_ID, NAME);
    }
}
