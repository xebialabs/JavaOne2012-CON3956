package com.xebialabs.j12012;

import org.testng.annotations.AfterClass;

import com.xebialabs.overcast.CloudHost;
import com.xebialabs.overcast.CloudHostFactory;

public abstract class OverthereScenarioWithOvercast extends OverthereScenario {
    protected CloudHost host;

    public void setupHost(String name) {
        host = CloudHostFactory.getCloudHost(name);
        host.setup();
    }

    @AfterClass
    public void teardownHost() {
        host.teardown();
    }

    @Override
    protected String getIp() {
        return host.getHostName();
    }
}
