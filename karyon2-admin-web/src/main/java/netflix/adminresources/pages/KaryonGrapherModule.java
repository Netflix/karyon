package netflix.adminresources.pages;

import com.google.inject.AbstractModule;
import com.google.inject.grapher.NameFactory;
import com.google.inject.grapher.ShortNameFactory;
import com.google.inject.grapher.graphviz.PortIdFactory;
import com.google.inject.grapher.graphviz.PortIdFactoryImpl;

public class KaryonGrapherModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(NameFactory.class).to(ShortNameFactory.class);
        bind(PortIdFactory.class).to(PortIdFactoryImpl.class);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

}
