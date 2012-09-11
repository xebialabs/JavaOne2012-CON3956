package com.xebialabs.j12012;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.Map;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import com.google.common.collect.Maps;

import com.xebialabs.overcast.CloudHost;
import com.xebialabs.overcast.CloudHostFactory;

public class SshScpScenario extends ScenarioTest {
    private CloudHost overthere_ssh;

    @BeforeClass
    public void setupHost() {
        overthere_ssh = CloudHostFactory.getCloudHost("overthere_ssh");
        overthere_ssh.setup();
    }

    @AfterClass
    public void teardownHost() {
        overthere_ssh.teardown();
    }

    @Override
    protected void initTarget() throws IOException {
        final Map<String, String> map = Collections.singletonMap("password", "overhere");
        targetFileSystem = FileSystems.newFileSystem(URI.create("ssh+scp://overthere@" + overthere_ssh.getHostName() + "/?os=UNIX"), map);
        targetCopyPathName = "/tmp";
        unzipPathName = "unzip";
    }

    @Override
    protected String getIp() {
        return overthere_ssh.getHostName();
    }
}
