package netflix.adminresources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.lifecycle.ClasspathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

@Singleton
public class AdminPageRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(AdminPageRegistry.class);
    public final static String PROP_ID_ADMIN_PAGES_SCAN = "netflix.platform.admin.pages.packages";
    public static final String DEFAULT_SCAN_PKG = "netflix";
    public final static String PROP_ID_ADMIN_PAGE_ANNOTATION = "netflix.platform.admin.pages.annotation";
    public static final String DEFAULT_ADMIN_PAGE_ANNOTATION = "netflix.adminresources.AdminPage";

    private Map<String, AdminPageInfo> baseServerPageInfoMap;
    private Injector injector;
    
    public AdminPageRegistry() {
        this.baseServerPageInfoMap = new ConcurrentHashMap<>();
        this.injector = null;
    }

    @Inject
    public AdminPageRegistry(Injector injector) {
        this.baseServerPageInfoMap = new ConcurrentHashMap<>();
        this.injector = injector;
    }
    
    public void add(AdminPageInfo baseServerPageInfo) {
        Preconditions.checkNotNull(baseServerPageInfo);
        baseServerPageInfoMap.put(baseServerPageInfo.getPageId(), baseServerPageInfo);
    }

    public void remove(AdminPageInfo baseServerPageInfo) {
        Preconditions.checkNotNull(baseServerPageInfo);
        baseServerPageInfoMap.remove(baseServerPageInfo.getPageId());
    }

    public AdminPageInfo getPageInfo(String pageId) {
        return baseServerPageInfoMap.get(pageId);
    }

    public Collection<AdminPageInfo> getAllPages() {
        List<AdminPageInfo> pages = Lists.newArrayList(baseServerPageInfoMap.values());
        List<AdminPageInfo> enabledPages = getEnabledPages(pages);

        // list needs to be sorted by page titles if available
        Collections.sort(enabledPages, new Comparator<AdminPageInfo>() {
            public int compare(AdminPageInfo left, AdminPageInfo right) {
                String leftVal = left.getName() != null ? left.getName() : left.getPageId();
                String rightVal = right.getName() != null ? right.getName() : right.getPageId();
                return leftVal.compareToIgnoreCase(rightVal);
            }
        });

        return enabledPages;
    }


    public void registerAdminPagesWithClasspathScan() {
        List<Class<? extends Annotation>> annotationList = getAdminPageAnnotations();
        ClasspathScanner cs = new ClasspathScanner(getAdminPagesPackagesToScan(), annotationList);
        for (Class<?> baseServerAdminPageClass : cs.getClasses()) {
            if (derivedFromAbstractBaseServePageInfo(baseServerAdminPageClass) ||
                    implementsAdminPageInfo(baseServerAdminPageClass)) {
                try {
                    add((AdminPageInfo) (injector == null ? baseServerAdminPageClass.newInstance() : injector.getInstance(baseServerAdminPageClass)));
                } catch (Exception e) {
                    LOG.warn(String.format("Exception registering %s admin page", baseServerAdminPageClass.getName()), e);
                }
            }
        }
    }

    public String buildJerseyResourcePkgListForAdminPages() {
        StringBuilder stringBuilder = new StringBuilder();
        Collection<AdminPageInfo> adminPages = getAllPages();
        int i = 0;
        for (AdminPageInfo adminPage : adminPages) {
            if (i > 0) {
                stringBuilder.append(";");
            }
            String pkgList = adminPage.getJerseyResourcePackageList();
            if (pkgList != null) {
                stringBuilder.append(pkgList);
            }
            i++;
        }
        return stringBuilder.toString();
    }

    private List<String> getAdminPagesPackagesToScan() {
        final String adminPagesPkgPath = ConfigurationManager.getConfigInstance().getString(PROP_ID_ADMIN_PAGES_SCAN, DEFAULT_SCAN_PKG);
        String[] pkgPaths = adminPagesPkgPath.split(";");
        return Lists.newArrayList(pkgPaths);
    }

    private boolean implementsAdminPageInfo(Class<?> baseServerAdminPageClass) {
        Class<?>[] interfacesImplemented = baseServerAdminPageClass.getInterfaces();

        for (Class<?> interfaceObj : interfacesImplemented) {
            if (interfaceObj.equals(AdminPageInfo.class)) {
                return true;
            }
        }
        return false;
    }

    private boolean derivedFromAbstractBaseServePageInfo(Class<?> baseServerAdminPageClass) {
        Class<?> superClass = baseServerAdminPageClass.getSuperclass();
        while (superClass != null) {
            if (superClass.equals(AbstractAdminPageInfo.class)) {
                return true;
            }
            superClass = superClass.getSuperclass();
        }
        return false;
    }

    public List<Class<? extends Annotation>> getAdminPageAnnotations() {
        final String adminPageAnnotationClasses = ConfigurationManager.getConfigInstance().getString(PROP_ID_ADMIN_PAGE_ANNOTATION, DEFAULT_ADMIN_PAGE_ANNOTATION);
        String[] clsNameList = adminPageAnnotationClasses.split(";");
        List<Class<? extends Annotation>> clsList = new ArrayList<>(clsNameList.length);
        for (String clsName : clsNameList) {
            try {
                final Class<?> aClass = Class.forName(clsName);
                if (aClass.isAnnotation()) {
                    clsList.add(aClass.asSubclass(Annotation.class));
                }
            } catch (ClassNotFoundException e) {
                LOG.warn("Invalid AdminPage Annotation class - " + clsName);
            }
        }
        return clsList;
    }

    private List<AdminPageInfo> getEnabledPages(List<AdminPageInfo> pages) {
        List<AdminPageInfo> enabledPages = Lists.newArrayList();
        for (AdminPageInfo page : pages) {
            if (page.isEnabled()) {
                enabledPages.add(page);
            }
        }
        return enabledPages;
    }
}

