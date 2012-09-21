package com.xebialabs.j12012;

import org.testng.annotations.AfterClass;
import com.google.common.io.Closeables;

public abstract class OverthereScenario extends Scenario {
    @AfterClass
    public void closeAll() {
        Closeables.closeQuietly(targetFileSystem);
    }


}
