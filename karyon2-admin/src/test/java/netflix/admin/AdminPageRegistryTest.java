package netflix.admin;

import com.google.inject.Module;
import com.netflix.config.ConfigurationManager;
import netflix.adminresources.AbstractAdminPageInfo;
import netflix.adminresources.AdminPage;
import netflix.adminresources.AdminPageInfo;
import netflix.adminresources.AdminPageRegistry;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class AdminPageRegistryTest {
    static final AdminPageRegistry adminPageRegistry = new AdminPageRegistry();

    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface MockAnnotation1 {
    }

    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public static @interface MockAnnotation2 {
    }

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
        public List<Module> getGuiceModules() {
            return new ArrayList<>(0);
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
    public void addAndRemoveAdminPluginDynamically() {
        Collection<AdminPageInfo> allPlugins = adminPageRegistry.getAllPages();
        assertThat("Admin plugins are not null by default", allPlugins, notNullValue());
        assertThat("Admin plugins size is 3", allPlugins.size(), is(3));

        final AbstractAdminPageInfo dynamicPlugin1 = new AbstractAdminPageInfo("dynamicPluginId1", "dynamicPluginName1") {
        };
        adminPageRegistry.add(dynamicPlugin1);
        assertThat("Admin plugins added dynamically", adminPageRegistry.getAllPages().size(), is(4));

        adminPageRegistry.remove(dynamicPlugin1);
        assertThat("Admin plugins removed dynamically", adminPageRegistry.getAllPages().size(), is(3));
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

    @Test
    public void defaultAdminPageAnnotations() {
        final List<Class<? extends Annotation>> adminPageAnnotations = adminPageRegistry.getAdminPageAnnotations();
        assertThat("AdminPage Annotations are not null by default", adminPageAnnotations, notNullValue());
        assertThat("AdminPage Annotations size is 1 by default", adminPageAnnotations.size(), is(1));
        final String defaultAnnotationClassName = adminPageAnnotations.get(0).getName();
        assertThat("DefaultAnnotationClassName is AdminPage", defaultAnnotationClassName, is("netflix.adminresources.AdminPage"));
    }


    @Test
    public void badAdminPageAnnotation() {
        ConfigurationManager.getConfigInstance().setProperty(AdminPageRegistry.PROP_ID_ADMIN_PAGE_ANNOTATION, "netflix.InvalidAnnotation1;netflix.InvalidAnnotation2");
        final List<Class<? extends Annotation>> adminPageAnnotations = adminPageRegistry.getAdminPageAnnotations();
        assertThat("AdminPage Annotations are not null", adminPageAnnotations, notNullValue());
        assertThat("AdminPage Annotations size is 0", adminPageAnnotations.size(), is(0));
        ConfigurationManager.getConfigInstance().setProperty(AdminPageRegistry.PROP_ID_ADMIN_PAGE_ANNOTATION, AdminPageRegistry.DEFAULT_ADMIN_PAGE_ANNOTATION);
    }

    @Test
    public void configureAdminPageAnnotation() {
        ConfigurationManager.getConfigInstance().setProperty(AdminPageRegistry.PROP_ID_ADMIN_PAGE_ANNOTATION,
                "netflix.admin.AdminPageRegistryTest$MockAnnotation1;netflix.admin.AdminPageRegistryTest$MockAnnotation2");
        final List<Class<? extends Annotation>> adminPageAnnotations = adminPageRegistry.getAdminPageAnnotations();
        assertThat("AdminPage Annotations are not empty", adminPageAnnotations, notNullValue());
        assertThat("AdminPage Annotations size is 2", adminPageAnnotations.size(), is(2));
        final String firstAnnotation = adminPageAnnotations.get(0).getName();
        final String secondAnnotation = adminPageAnnotations.get(1).getName();
        assertThat("AdminPage annotation is netflix.Annotation1", firstAnnotation, is("netflix.admin.AdminPageRegistryTest$MockAnnotation1"));
        assertThat("AdminPage annotation is netflix.Annotation2", secondAnnotation, is("netflix.admin.AdminPageRegistryTest$MockAnnotation2"));
        ConfigurationManager.getConfigInstance().setProperty(AdminPageRegistry.PROP_ID_ADMIN_PAGE_ANNOTATION, AdminPageRegistry.DEFAULT_ADMIN_PAGE_ANNOTATION);
    }

}
