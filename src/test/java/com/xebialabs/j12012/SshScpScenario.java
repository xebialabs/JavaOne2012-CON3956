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

public class SshScpScenario extends ScenarioTest {
    private CloudHost glassfish_ssh;

    @BeforeClass
    public void setupHost() {
        glassfish_ssh = CloudHostFactory.getCloudHost("glassfish_ssh");
        glassfish_ssh.setup();
    }

    @AfterClass
    public void teardownHost() {
        glassfish_ssh.teardown();
    }

    @Override
    protected void initTarget() throws IOException {
        final Map<String, String> map = Collections.singletonMap("password", "overhere");
        targetFileSystem = FileSystems.newFileSystem(URI.create("ssh+scp://overthere@" + glassfish_ssh.getHostName() + "/?os=UNIX"), map);
        targetCopyPathName = "/tmp";
        unzipPathName = "unzip";
    }

    @Override
    protected String getIp() {
        return glassfish_ssh.getHostName();
    }

    @Test
    public void neededForTestNGPluginForEclipseToRecognizeThisClassAsATest() { }

}
