package netflix.adminresources.pages;

import com.google.inject.Module;

import java.util.Arrays;
import java.util.List;

import netflix.adminresources.AbstractAdminPageInfo;
import netflix.adminresources.AdminPage;

@AdminPage
public class GuiceGraphPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "guicegraph";
    public static final String NAME = "GuiceGraph";

    public GuiceGraphPage() {
        super(PAGE_ID, NAME);
    }
    
    @Override
    public List<Module> getGuiceModules() {
        return Arrays.<Module>asList(new KaryonGrapherModule());
    }
}
