package com.xebialabs.j12012;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import org.testng.annotations.*;

import com.google.common.io.Closeables;

import com.xebialabs.overthere.OverthereProcess;

import static com.xebialabs.j12012.Commons.isReachable;
import static com.xebialabs.overthere.nio.process.Processes.execute;
import static com.xebialabs.overthere.nio.process.Processes.startProcess;
import static java.nio.file.Files.copy;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class ScenarioTest {
    public static final String GLASSFISH_ZIP = "glassfish-3.1.2.2.zip";
    public static final String PETCLINIC_EAR = "PetClinic-1.0.ear";
    public static final String GLASSFISH_DIR = "glassfish3";

    private FileSystem localFileSystem;
    protected FileSystem targetFileSystem;
    protected String targetCopyPathName;
    protected String unzipPathName;
    private Path unzipPath;
    private Path targetGlassfishDirPath;
    private Path localGlassfishZipPath;
    private Path localPetClinicEarPath;
    private Path targetGlassfishZipPath;
    private Path targetPetClinicDeployPath;

    @BeforeMethod
    public void setup() throws IOException {
        initTarget();
        localFileSystem = FileSystems.getDefault();

        localGlassfishZipPath = localFileSystem.getPath(GLASSFISH_ZIP);
        localPetClinicEarPath = localFileSystem.getPath(PETCLINIC_EAR);

        unzipPath = targetFileSystem.getPath(unzipPathName);
        targetGlassfishZipPath = targetFileSystem.getPath(targetCopyPathName, GLASSFISH_ZIP);
        targetGlassfishDirPath = targetFileSystem.getPath(targetCopyPathName, GLASSFISH_DIR);
        targetPetClinicDeployPath = targetGlassfishDirPath.resolve("glassfish/domains/domain1/autodeploy").resolve(PETCLINIC_EAR);
    }

    @BeforeMethod(dependsOnMethods = "setup")
    public void cleanup() throws IOException {
        if (Commons.isReachable(getIp(), 4848)) {
            execute(targetGlassfishDirPath.resolve("bin/asadmin"), "stop-domain");
        }

        Files.walkFileTree(targetGlassfishDirPath, new DeleteDirVisitor());
        Files.deleteIfExists(targetFileSystem.getPath(targetCopyPathName, GLASSFISH_ZIP));
    }

    @AfterMethod
    public void closeAll() {
        Closeables.closeQuietly(targetFileSystem);
    }

    protected abstract void initTarget() throws IOException;

    protected abstract String getIp();

    @Test
    public void shouldCopyToRemote() throws IOException, InterruptedException {
        // Copy GlassFish zip to remote machine.
        assertThat("Glassfish should not exist before copy", !Files.exists(targetGlassfishZipPath));
        copy(localGlassfishZipPath, targetGlassfishZipPath, StandardCopyOption.REPLACE_EXISTING);
        assertThat("Glassfish should exist after copy", Files.exists(targetGlassfishZipPath));

        // Unzip GlassFish zip on remote machine
        assertThat("Glassfish dir should not exist before unzip", !Files.exists(targetGlassfishDirPath));
        execute(unzipPath, targetGlassfishZipPath.toString(), "-d", targetGlassfishDirPath.getParent().toString());
        assertThat("Glassfish dir should exist after unzip", Files.exists(targetGlassfishDirPath));

        // Start GlassFish container
        assertThat("GlassFish should not be started yet", !Commons.isReachable(getIp(), 4848));
        OverthereProcess overthereProcess = startProcess(targetGlassfishDirPath.resolve("glassfish/bin/startserv"));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(overthereProcess.getStderr()));
        String line;
        System.out.println("------------------------- Glassfish boot log -------------------------");
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
            if (line.contains("INFO: Successfully launched")) {
                break;
            }
        }
        System.out.println("----------------------------------------------------------------------");

        Commons.waitUntilReachable(getIp(), 4848);

        // Deploy PetClinic-1.0.ear on GlassFish.
        copy(localPetClinicEarPath, targetPetClinicDeployPath, StandardCopyOption.REPLACE_EXISTING);
        try {
            Commons.waitUntilButtonClicked();
        } finally {
            // Kill GlassFish process.
            overthereProcess.destroy();
        }


        Files.walkFileTree(targetGlassfishDirPath, new DeleteDirVisitor());
        Files.deleteIfExists(targetFileSystem.getPath(targetCopyPathName, GLASSFISH_ZIP));
    }

}
