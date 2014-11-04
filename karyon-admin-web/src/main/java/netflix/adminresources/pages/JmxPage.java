package netflix.adminresources.pages;

import netflix.adminresources.AbstractAdminPageInfo;
import netflix.adminresources.AdminPage;

@AdminPage
public class JmxPage extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "jmx";
    public static final String NAME = "JMX";

    public JmxPage() {
        super(PAGE_ID, NAME);
    }
}
