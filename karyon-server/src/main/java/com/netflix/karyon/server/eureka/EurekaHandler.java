package com.netflix.karyon.server.eureka;

import com.google.inject.Inject;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.CloudInstanceConfig;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.governator.annotations.AutoBindSingleton;
import com.netflix.governator.annotations.Configuration;
import com.netflix.karyon.server.utils.KaryonUtils;
import com.netflix.karyon.spi.PropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

import static com.netflix.karyon.spi.PropertyNames.EUREKA_DATACENTER_TYPE_PROP_NAME;
import static com.netflix.karyon.spi.PropertyNames.EUREKA_PROPERTIES_NAME_PREFIX_PROP_NAME;

/**
 * A handler for integrating with <a href="https://github.com/Netflix/eureka/">Eureka</a>. This handler can be disabled
 * by setting a property {@link PropertyNames#KARYON_PROPERTIES_PREFIX}.{@link PropertyNames#EUREKA_COMPONENT_NAME}.disabled
 * as <code>true</code>. <br/>
 * If enabled, this class registers the application with eureka as a cloud instance iff the value of the property
 * {@link PropertyNames#EUREKA_DATACENTER_TYPE_PROP_NAME} is set to {@link DataCenterInfo.Name#Amazon} or
 * {@link DataCenterInfo.Name#Netflix}. In such a case, this class uses {@link CloudInstanceConfig} to register with
 * eureka. Otherwise {@link MyDataCenterInstanceConfig} is used as eureka config.
 *
 * @author Nitesh Kant (nkant@netflix.com)
 * @see com.netflix.karyon.server.KaryonServer
 */
@AutoBindSingleton
public class EurekaHandler {

    protected static final Logger logger = LoggerFactory.getLogger(EurekaHandler.class);

    private EurekaHealthCheckCallback eurekaHealthCheckCallback;

    @Configuration(
            value = EUREKA_PROPERTIES_NAME_PREFIX_PROP_NAME,
            documentation = "Namespace for eureka related properties."
    )
    private String eurekaNamespace = "eureka";

    @Configuration(
            value = EUREKA_DATACENTER_TYPE_PROP_NAME,
            documentation = "Datacenter type used for initializing appropriate eureka instance configuration."
    )
    private String datacenterType;

    @Inject
    public EurekaHandler(EurekaHealthCheckCallback eurekaHealthCheckCallback) {
        this.eurekaHealthCheckCallback = eurekaHealthCheckCallback;
    }

    @PostConstruct
    public void postConfig() {
        if (!eurekaNamespace.endsWith(".")) {
            eurekaNamespace = eurekaNamespace + "."; // Eureka requires this.
        }
    }

    public void register() {
        if (!KaryonUtils.isCoreComponentEnabled(PropertyNames.EUREKA_COMPONENT_NAME)) {
            logger.info("Eureka is disabled, skipping instance's eureka registration.");
            return;
        }

        EurekaInstanceConfig eurekaInstanceConfig;

        DataCenterInfo.Name dcType = DataCenterInfo.Name.MyOwn;
        if (null != datacenterType) {
            try {
                dcType = DataCenterInfo.Name.valueOf(datacenterType);
            } catch (IllegalArgumentException e) {
                logger.warn(String.format(
                        "Illegal value %s for eureka datacenter provided in property %s. Ignoring the same and defaulting to %s",
                        datacenterType, EUREKA_DATACENTER_TYPE_PROP_NAME, DataCenterInfo.Name.MyOwn));
            }
        }

        switch (dcType) {
            case Netflix:
            case Amazon:
                eurekaInstanceConfig = new CloudInstanceConfig(eurekaNamespace);
                break;
            default:
                // Every other value is just custom data center.
                eurekaInstanceConfig = new MyDataCenterInstanceConfig(eurekaNamespace);
                break;
        }

        DiscoveryManager.getInstance().initComponent(eurekaInstanceConfig, new DefaultEurekaClientConfig(eurekaNamespace));
        DiscoveryManager.getInstance().getDiscoveryClient().registerHealthCheckCallback(eurekaHealthCheckCallback);
    }

    public void markAsUp() {
        if (!KaryonUtils.isCoreComponentEnabled(PropertyNames.EUREKA_COMPONENT_NAME)) {
            logger.info("Eureka is disabled, skipping instance's eureka update to up.");
            return;
        }

        ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.UP);
    }

    public void markAsDown() {
        if (!KaryonUtils.isCoreComponentEnabled(PropertyNames.EUREKA_COMPONENT_NAME)) {
            logger.info("Eureka is disabled, skipping instance's eureka update to down.");
            return;
        }

        ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.DOWN);
    }
}
