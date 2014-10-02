package com.netflix.adminresources.pages;

import com.netflix.adminresources.AbstractAdminPageInfo;
import com.netflix.adminresources.AdminPage;

@AdminPage
public class EurekaPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "eureka";
    public static final String NAME = "Eureka";

    public EurekaPage() {
        super(PAGE_ID, NAME);
    }
}