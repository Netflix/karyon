package com.netflix.adminresources.pages;


import com.netflix.adminresources.AbstractAdminPageInfo;
import com.netflix.adminresources.AdminPage;

@AdminPage
public class EnvPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "env";
    public static final String NAME = "Environment";

    public EnvPage() {
        super(PAGE_ID, NAME);
    }
}
