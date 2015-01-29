package netflix.admin;

import netflix.adminresources.AbstractAdminPageInfo;
import netflix.adminresources.AdminPage;
import netflix.adminresources.AdminPageInfo;
import netflix.adminresources.AdminPageRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class AdminPageRegistryTest {
    static final AdminPageRegistry adminPageRegistry = new AdminPageRegistry();

    @AdminPage
    public static class MockPlugin1 extends AbstractAdminPageInfo {
        public MockPlugin1() {
            super("id-plugin1", "plugin-1");
        }

        @Override
        public String getJerseyResourcePackageList() {
            return "pkg1;pkg2";
        }

    }

    @AdminPage
    public static class MockPlugin2 extends AbstractAdminPageInfo {
        public MockPlugin2() {
            super("id-plugin2", "plugin-2");
        }

        @Override
        public String getJerseyResourcePackageList() {
            return "pkg3;pkg4";
        }
    }

    @AdminPage
    public static class DisabledMockPlugin3 extends AbstractAdminPageInfo {
        public DisabledMockPlugin3() {
            super("id-plugin3", "plugin-3");
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    @AdminPage
    public static class DisabledMockPlugin4 implements AdminPageInfo {
        private String id = "id-plugin4";
        private String name = "plugin4";

        @Override
        public String getPageId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getPageTemplate() {
            return null;
        }

        @Override
        public Map<String, Object> getDataModel() {
            Map<String, Object> dataModel = new HashMap<>(1);
            dataModel.put("foo", "bar");
            return dataModel;
        }

        @Override
        public String getJerseyResourcePackageList() {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public boolean isVisible() {
            return false;
        }
    }

    @Before
    public void init() {
        adminPageRegistry.registerAdminPagesWithClasspathScan();
    }

    @Test
    public void verifyAllPages() {
        final Collection<AdminPageInfo> allPages = adminPageRegistry.getAllPages();
        // 4 modules - 1 disabled
        assertTrue(allPages.size() == 3);
        for (AdminPageInfo pageInfo : allPages) {
            assertTrue(pageInfo.getName().startsWith("plugin"));
        }
    }

    @Test
    public void verifyJerseyPkgPath() {
        final String jerseyResourcePath = adminPageRegistry.buildJerseyResourcePkgListForAdminPages();
        assertEquals("pkg1;pkg2;pkg3;pkg4;", jerseyResourcePath);
    }

    @Test
    public void verifyVisible() {
        final AdminPageInfo plugin4 = adminPageRegistry.getPageInfo("id-plugin4");
        assertThat("Plugin4 should not be null", plugin4, notNullValue());
        assertThat("Plugin4 should not be visible", plugin4.isVisible(), is(false));
    }

    @Test
    public void verifyDataModel() {
        final AdminPageInfo plugin4 = adminPageRegistry.getPageInfo("id-plugin4");
        assertThat("Plugin4 should is null", plugin4, notNullValue());
        final Map<String, Object> dataModel = plugin4.getDataModel();
        assertThat("Data model is null", dataModel, notNullValue());
        assertThat("Data model does not contain 1 entry", dataModel.size(), is(1));
        assertThat("Data model does not contain foo", dataModel.containsKey("foo"), is(true));
    }

}
