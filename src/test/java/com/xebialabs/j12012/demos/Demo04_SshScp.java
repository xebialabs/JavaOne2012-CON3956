package com.xebialabs.j12012.demos;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.Map;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.xebialabs.j12012.OverthereScenarioWithOvercast;
import com.xebialabs.j12012.Scenario;
import com.xebialabs.overcast.CloudHost;
import com.xebialabs.overcast.CloudHostFactory;

public class Demo04_SshScp extends OverthereScenarioWithOvercast {
    @Override
    protected void initTarget() throws IOException {
        setupHost("glassfish_ssh");
        final Map<String, String> map = Collections.singletonMap("password", "overhere");
        targetFileSystem = FileSystems.newFileSystem(URI.create("ssh+scp://overthere@" + host.getHostName() + "/?os=UNIX"), map);
        targetCopyPathName = "/tmp";
        unzipPathName = "unzip";
    }

    @Test
    public void neededForTestNGPluginForEclipseToRecognizeThisClassAsATest() { }

}
