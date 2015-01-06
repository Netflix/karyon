package netflix.karyon.eureka;

import com.netflix.appinfo.InstanceInfo;

/**
 * @author Nitesh Kant
 */
public class DefaultEurekaKaryonStatusBridge implements EurekaKaryonStatusBridge {

    @Override
    public InstanceInfo.InstanceStatus interpretKaryonStatus(int karyonStatus) {
        if(karyonStatus == 204) {
            return InstanceInfo.InstanceStatus.STARTING;
        } else if (karyonStatus >= 200 && karyonStatus < 300) {
            return InstanceInfo.InstanceStatus.UP;
        }

        return InstanceInfo.InstanceStatus.DOWN;
    }
}
