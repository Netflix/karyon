package com.netflix.adminresources;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.explorers.AbstractExplorerModule;
import com.netflix.explorers.ExplorerManager;

import javax.annotation.PostConstruct;

@Singleton
public class AdminResourceExplorer extends AbstractExplorerModule {

    @Inject
    public AdminResourceExplorer(ExplorerManager manager) {
        super("admin");
        manager.registerExplorer(this);
    }

    @Override
    public String getTitle() {
        return "Karyon Admin";
    }

    @PostConstruct
    public void initialize() {
        super.initialize();
    }
}
