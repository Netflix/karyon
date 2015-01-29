package netflix.adminresources.pages;


import netflix.adminresources.AbstractAdminPageInfo;
import netflix.adminresources.AdminPage;

@AdminPage
public class EnvPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "env";
    public static final String NAME = "Environment";

    public EnvPage() {
        super(PAGE_ID, NAME);
    }
}
