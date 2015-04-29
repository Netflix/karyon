package netflix.adminresources;

@AdminPage
public class HealthCheckPlugin extends AbstractAdminPageInfo {

    public static final String PAGE_ID = "karyon_healthCheck";
    public static final String NAME = "HealthCheck";

    public HealthCheckPlugin() {
        super(PAGE_ID, NAME);
    }

    @Override
    public String getJerseyResourcePackageList() {
        return "com.netflix.adminresources";
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}
