package com.xebialabs.j12012;

import java.io.IOException;
import java.nio.file.*;
import org.testng.annotations.*;

import com.google.common.io.Closeables;

import com.xebialabs.overthere.nio.process.Processes;

import static com.xebialabs.j12012.ConnectionTester.isReachable;
import static com.xebialabs.overthere.nio.process.Processes.execute;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isReadable;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class ScenarioTest {
    public static final String GLASSFISH_ZIP = "glassfish-3.1.2.2-web.zip";
    public static final String GLASSFISH_DIR = "glassfish3";

    private FileSystem localFileSystem;
    protected FileSystem targetFileSystem;
    protected String targetCopyPathName;
    private Path targetCopyPath;
    protected String unzipPathName;
    private Path unzipPath;
    private Path glassfishPath;

    @BeforeMethod
    public void setup() throws IOException {
        initTarget();
        localFileSystem = FileSystems.getDefault();
        targetCopyPath = targetFileSystem.getPath(targetCopyPathName);
        unzipPath = targetFileSystem.getPath(unzipPathName);
        glassfishPath = targetCopyPath.resolve(GLASSFISH_DIR);
    }

    @AfterMethod
    public void closeAll() {
        Closeables.closeQuietly(targetFileSystem);
    }

    protected abstract void initTarget() throws IOException;

    protected abstract String getIp();

    @Test
    public void cleanup() throws IOException {
        if (isReachable(getIp(), 4848)) {
            execute(glassfishPath.resolve("bin/asadmin"), "stop-domain");
        }

//        Files.walkFileTree(glassfishPath, new DeleteDirVisitor());
        Files.delete(targetFileSystem.getPath(targetCopyPathName, GLASSFISH_ZIP));
    }

    @Test(dependsOnMethods = "cleanup")
    public void shouldCopyToRemote() throws IOException {
        Path localPath = localFileSystem.getPath(GLASSFISH_ZIP);
        Path targetPath = targetFileSystem.getPath(targetCopyPathName, GLASSFISH_ZIP);

        assertThat("Glassfish should not exist before copy", !Files.exists(targetPath));
        copy(localPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        assertThat("Glassfish should exist after copy", Files.exists(targetPath));

        assertThat("Glassfish dir should not exist before unzip", !Files.exists(glassfishPath));
        execute(targetFileSystem.getPath(unzipPathName), targetPath.toString(), "-d", glassfishPath.getParent().toString());
        assertThat("Glassfish dir should exist after unzip", Files.exists(glassfishPath));

        assertThat("GlassFish should not be started yet", !isReachable(getIp(), 4848));
        execute(glassfishPath.resolve("bin/asadmin"), "start-domain");
        assertThat("GlassFish should be started", isReachable(getIp(), 4848));
    }

}
