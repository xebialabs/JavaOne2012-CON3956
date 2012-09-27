package com.xebialabs.j12012;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.nio.process.Processes;

import static com.xebialabs.j12012.Commons.dumpStream;
import static com.xebialabs.j12012.Commons.waitUntilButtonClicked;
import static com.xebialabs.j12012.Commons.waitUntilReachable;
import static java.lang.System.out;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class Scenario {
    public static final String GLASSFISH_ZIP = "glassfish-3.1.2.2.zip";
    public static final String PETCLINIC_EAR = "PetClinic-1.0.ear";
    public static final String GLASSFISH_DIR = "glassfish3";

    private FileSystem localFileSystem;
    protected FileSystem targetFileSystem;
    protected String targetCopyPathName;
    protected String unzipPathName;
    protected Path unzipPath;
    protected Path targetGlassFishDirPath;
    private Path localGlassFishZipPath;
    private Path localPetClinicEarPath;
    protected Path targetGlassFishZipPath;
    private Path targetPetClinicDeployPath;
    private OverthereProcess overthereProcess;
    protected URI adminURI;
    private URI petClinicURI;

    @BeforeClass
    public void setup() throws IOException {
        initTarget();
        localFileSystem = FileSystems.getDefault();

        localGlassFishZipPath = localFileSystem.getPath(GLASSFISH_ZIP);
        localPetClinicEarPath = localFileSystem.getPath(PETCLINIC_EAR);

        unzipPath = targetFileSystem.getPath(unzipPathName);
        targetGlassFishZipPath = targetFileSystem.getPath(targetCopyPathName, GLASSFISH_ZIP);
        targetGlassFishDirPath = targetFileSystem.getPath(targetCopyPathName, GLASSFISH_DIR);
        targetPetClinicDeployPath = targetGlassFishDirPath.resolve("glassfish/domains/domain1/autodeploy").resolve(PETCLINIC_EAR);

        adminURI = URI.create(String.format("http://%s:4848/", getIp()));
        petClinicURI = URI.create(String.format("http://%s:8080/petclinic", getIp()));
    }

    @BeforeClass(dependsOnMethods = "setup")
    public void cleanup() throws IOException {
        if (Commons.isReachable(adminURI)) {
            Processes.execute(targetGlassFishDirPath.resolve("bin/asadmin"), "stop-domain");
        }

        if (Files.exists(targetGlassFishDirPath)) {
            Files.walkFileTree(targetGlassFishDirPath, new DeleteDirVisitor());
        }
        Files.deleteIfExists(targetFileSystem.getPath(targetCopyPathName, GLASSFISH_ZIP));
    }

    protected abstract void initTarget() throws IOException;

    protected abstract String getIp();

    @Test
    public void copyGlassFishZip() throws IOException {
        out.println("*** Copying GlassFish zip");
        assertThat("GlassFish should not exist before copy", !Files.exists(targetGlassFishZipPath));
        Files.copy(localGlassFishZipPath, targetGlassFishZipPath, StandardCopyOption.REPLACE_EXISTING);
        assertThat("GlassFish should exist after copy", Files.exists(targetGlassFishZipPath));
    }

    @Test(dependsOnMethods = "copyGlassFishZip")
    public void unzipGlassFishZip() throws IOException, InterruptedException {
        out.println("*** Unzipping GlassFish zip");
        assertThat("GlassFish dir should not exist before unzip", !Files.exists(targetGlassFishDirPath));
        Processes.execute(unzipPath, targetGlassFishZipPath.toString(), "-d", targetGlassFishDirPath.getParent().toString());
        assertThat("GlassFish dir should exist after unzip", Files.exists(targetGlassFishDirPath));
    }

    @Test(dependsOnMethods = "unzipGlassFishZip")
    public void startGlassFishContainer() throws IOException, InterruptedException {
        // Start GlassFish container
        assertThat("GlassFish should not be started yet", !Commons.isReachable(adminURI));
        overthereProcess = Processes.startProcess(targetGlassFishDirPath.resolve("glassfish/bin/startserv"));
        dumpStream(overthereProcess.getStdout(), System.out);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(overthereProcess.getStderr()));
        String line;
        out.println("------------------------- GlassFish boot log -------------------------");
        while ((line = bufferedReader.readLine()) != null) {
            out.println(line);
            if (line.contains("INFO: Successfully launched")) {
                break;
            }
        }
        dumpStream(overthereProcess.getStderr(), System.err);
        System.out.println("----------------------------------------------------------------------");

        waitUntilReachable(adminURI);
        assertThat("GlassFish should not be started yet", Commons.isReachable(adminURI));
    }

    @Test(dependsOnMethods = "startGlassFishContainer")
    public void deployPetClinicEar() throws IOException, InterruptedException {
        out.println("*** Deploying PetClinic ear to GlassFish");

        assertThat(Commons.getStatusCode(petClinicURI), equalTo(404));
        Files.copy(localPetClinicEarPath, targetPetClinicDeployPath, StandardCopyOption.REPLACE_EXISTING);
        waitUntilReachable(petClinicURI);
        assertThat(Commons.getStatusCode(petClinicURI), equalTo(200));
    }

    @Test(dependsOnMethods = "deployPetClinicEar")
    public void waitForButton() throws IOException, InterruptedException {
        out.println("*** Pausing for button click");
        waitUntilButtonClicked(petClinicURI.toString());
    }

    @Test(dependsOnMethods = "waitForButton")
    public void stopGlassFishContainer() throws InterruptedException, IOException {
        out.println("*** Stopping GlassFish container");
        Processes.execute(targetGlassFishDirPath.resolve("bin/asadmin"), "stop-domain");
        overthereProcess.waitFor();
    }

    @Test(dependsOnMethods = "stopGlassFishContainer")
    public void removeCopiedFiles() throws IOException, InterruptedException {
        out.println("*** Cleaning up copied files");
        // Clean up, but first wait a bit for the file descriptors to clear.
        Thread.sleep(2000);
        Files.walkFileTree(targetGlassFishDirPath, new DeleteDirVisitor());
        Files.deleteIfExists(targetGlassFishZipPath);
    }


}

