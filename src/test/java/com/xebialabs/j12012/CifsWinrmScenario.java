package com.xebialabs.j12012;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.xebialabs.overcast.CloudHost;
import com.xebialabs.overcast.CloudHostFactory;

public class CifsWinrmScenario extends ScenarioTest {
    private CloudHost host;

    @BeforeClass
    public void setupHost() {
        host = CloudHostFactory.getCloudHost("glassfish_cifs");
        host.setup();
    }

    @AfterClass
    public void teardownHost() {
        host.teardown();
    }

    @Override
    protected void initTarget() throws IOException {
        final Map<String, String> map = Collections.singletonMap("password", "xeb1aLabs");
        targetFileSystem = FileSystems.newFileSystem(URI.create("cifs://Administrator@" + host.getHostName() + "/?os=WINDOWS&connectionType=WINRM"), map);
        targetCopyPathName = "C:\\temp";
        unzipPathName = "C:\\temp\\unzip.exe";
    }

    @Override
    protected String getIp() {
        return host.getHostName();
    }

    @Test
    public void neededForTestNGPluginForEclipseToRecognizeThisClassAsATest() { }

}
