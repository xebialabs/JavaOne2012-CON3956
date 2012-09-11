package com.xebialabs.j12012;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import com.google.common.collect.Maps;

public class LocalScenario extends ScenarioTest {
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
}
