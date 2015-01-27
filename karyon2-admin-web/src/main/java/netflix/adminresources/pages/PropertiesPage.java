package netflix.adminresources.pages;


import netflix.adminresources.AbstractAdminPageInfo;
import netflix.adminresources.AdminPage;

@AdminPage
public class PropertiesPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "archprops";
    public static final String NAME = "Archaius";

    public PropertiesPage() {
        super(PAGE_ID, NAME);
    }
}
