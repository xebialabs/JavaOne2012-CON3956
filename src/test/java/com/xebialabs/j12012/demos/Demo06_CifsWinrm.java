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

public class Demo06_CifsWinrm extends OverthereScenarioWithOvercast {
    @Override
    protected void initTarget() throws IOException {
        setupHost("glassfish_cifs");
        final Map<String, String> map = Collections.singletonMap("password", "xeb1aLabs");
        targetFileSystem = FileSystems.newFileSystem(URI.create("cifs+winrm://Administrator@" + host.getHostName() + "/"), map);
        targetCopyPathName = "C:\\temp";
        unzipPathName = "C:\\temp\\unzip.exe";
    }

    @Test
    public void neededForTestNGPluginForEclipseToRecognizeThisClassAsATest() { }

}
