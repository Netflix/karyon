package netflix.admin;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.explorers.providers.FreemarkerTemplateProvider;
import com.sun.jersey.api.view.Viewable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class AdminFreemarkerTemplateProvider implements MessageBodyWriter<Viewable> {
    private static final Logger LOG = LoggerFactory.getLogger(AdminFreemarkerTemplateProvider.class);
    private FreemarkerTemplateProvider freemarkerTemplateProvider;
    private AtomicBoolean requestInvokerInjected = new AtomicBoolean(false);

    @Context
    private ThreadLocal<HttpServletRequest> requestInvoker;

    @Inject
    public AdminFreemarkerTemplateProvider(AdminExplorerManager adminExplorerManager) {
        try {
            freemarkerTemplateProvider = new FreemarkerTemplateProvider();
            final Class<? extends FreemarkerTemplateProvider> ftpClass = freemarkerTemplateProvider.getClass();
            final Field managerField = ftpClass.getDeclaredField("manager");
            managerField.setAccessible(true);
            managerField.set(freemarkerTemplateProvider, adminExplorerManager);
            LOG.info("AdminFreemarkerTemplateProvider created");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error("Failed in reflection for AdminFreemarkerTemplateProvider - ");
            throw new RuntimeException("Can not inject AdminExplorerManager in AdminFreemarkerTemplateProvider");
        }
    }

    @PostConstruct
    public void initialize() {
        if (freemarkerTemplateProvider != null) {
            freemarkerTemplateProvider.commonConstruct();
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return freemarkerTemplateProvider.isWriteable(type, genericType, annotations, mediaType);
    }

    @Override
    public long getSize(Viewable viewable, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return freemarkerTemplateProvider.getSize(viewable, type, genericType, annotations, mediaType);
    }

    @Override
    public void writeTo(Viewable viewable, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        injectRequestInvoker();
        freemarkerTemplateProvider.writeTo(viewable, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    private void injectRequestInvoker() {
        if (requestInvokerInjected.compareAndSet(false, true)) {
            final Class<? extends FreemarkerTemplateProvider> ftpClass = freemarkerTemplateProvider.getClass();
            try {
                final Field requestInvokerField = ftpClass.getDeclaredField("requestInvoker");
                requestInvokerField.setAccessible(true);
                requestInvokerField.set(freemarkerTemplateProvider, requestInvoker);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                LOG.error("Failed in inject requestInvoker in FreemarkerTemplateProvider - ");
            }
        }
    }
}

