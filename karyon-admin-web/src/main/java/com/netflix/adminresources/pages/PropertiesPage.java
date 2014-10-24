package com.netflix.adminresources.pages;


import com.netflix.adminresources.AbstractAdminPageInfo;
import com.netflix.adminresources.AdminPage;

@AdminPage
public class PropertiesPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "props";
    public static final String NAME = "Archaius";

    public PropertiesPage() {
        super(PAGE_ID, NAME);
    }
}
