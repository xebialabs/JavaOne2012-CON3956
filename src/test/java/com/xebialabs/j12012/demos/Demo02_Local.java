package com.xebialabs.j12012.demos;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;

import org.testng.annotations.Test;

import com.google.common.collect.Maps;

import com.xebialabs.j12012.OverthereScenario;
import com.xebialabs.j12012.Scenario;

public class Demo02_Local extends OverthereScenario {
    @Override
    protected void initTarget() throws IOException {
        targetFileSystem = FileSystems.newFileSystem(URI.create("local:///"), Maps.<String, Object>newHashMap());
        targetCopyPathName = "/tmp";
        unzipPathName = "unzip";
    }

    @Override
    protected String getIp() {
        return "127.0.0.1";
    }
    
    @Test
    public void neededForTestNGPluginForEclipseToRecognizeThisClassAsATest() { }

}
