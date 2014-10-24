package com.netflix.adminresources;

import com.netflix.config.ConfigurationManager;

public abstract class AbstractAdminPageInfo implements AdminPageInfo {
    public static final String ADMIN_PAGE_DISABLE_PROP_PREFIX = "netflix.platform.admin.pages.";
    public static final String DISABLED = ".disabled";

    private final String pageId;
    private final String name;

    public AbstractAdminPageInfo(String pageId, String name) {
        this.pageId = pageId;
        this.name = name;
    }

    public String getPageId() {
        return pageId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPageTemplate() {
        return "/webadmin/" + pageId + "/index.ftl";
    }

    @Override
    public String getJerseyResourcePackageList() {
        return "";
    }

    @Override
    public boolean isEnabled() {
        final String disablePagePropId = ADMIN_PAGE_DISABLE_PROP_PREFIX + pageId + DISABLED;
        boolean isDisabled = ConfigurationManager.getConfigInstance().getBoolean(disablePagePropId, false);
        return !isDisabled;
    }
}

