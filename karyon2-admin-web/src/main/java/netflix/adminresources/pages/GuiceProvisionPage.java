package netflix.adminresources.pages;

import com.google.inject.Module;

import java.util.Arrays;
import java.util.List;

import netflix.adminresources.AbstractAdminPageInfo;
import netflix.adminresources.AdminPage;

@AdminPage
public class GuiceProvisionPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "guiceprovision";
    public static final String NAME = "GuiceProvision";

    public GuiceProvisionPage() {
        super(PAGE_ID, NAME);
    }
    
    @Override
    public List<Module> getGuiceModules() {
        return Arrays.<Module>asList(new KaryonGrapherModule());
    }
}
