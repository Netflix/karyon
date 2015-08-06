package com.netflix.karyon.eureka;

import com.netflix.appinfo.DataCenterInfo;

public final class DataCenterInfos {
    public static DataCenterInfo custom() {
        return new DataCenterInfo() {
            @Override
            public Name getName() {
                return DataCenterInfo.Name.MyOwn;
            }
        };
    }
}
