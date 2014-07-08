package com.netflix.hellonoss.server;

import com.netflix.adminresources.resources.KaryonWebAdminModule;
import com.netflix.governator.annotations.Modules;
import com.netflix.hellonoss.server.auth.AuthInterceptor;
import com.netflix.hellonoss.server.auth.AuthenticationService;
import com.netflix.hellonoss.server.auth.AuthenticationServiceImpl;
import com.netflix.karyon.KaryonBootstrap;
import com.netflix.karyon.archaius.Archaius;
import com.netflix.karyon.server.http.jersey.blocking.KaryonJerseyModule;
import com.netflix.karyon.transport.http.GovernatorHttpInterceptorSupport;
import io.netty.buffer.ByteBuf;

import javax.inject.Inject;

/**
 * @author Nitesh Kant
 */
@Archaius
@KaryonBootstrap(name = "hello-netflix-oss", port = 8888, shutdownPort = 9999)
@Modules(include = {HelloNossApp.KaryonJerseyModuleImpl.class, KaryonWebAdminModule.class})
public final class HelloNossApp  {

    public class KaryonJerseyModuleImpl extends KaryonJerseyModule {

        @Inject
        public KaryonJerseyModuleImpl(KaryonBootstrap karyonBootstrap) {
            super(karyonBootstrap);
        }

        @Override
        protected void configure() {
            super.configure();
            bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
        }

        @Override
        public void configureInterceptors(GovernatorHttpInterceptorSupport<ByteBuf, ByteBuf> interceptorSupport) {
            interceptorSupport.forUri("/*").intercept(LoggingInterceptor.class);
            interceptorSupport.forUri("/hello").interceptIn(AuthInterceptor.class);
        }
    }
}
