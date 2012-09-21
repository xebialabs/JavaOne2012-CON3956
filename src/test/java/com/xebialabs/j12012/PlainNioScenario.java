package com.xebialabs.j12012;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.common.io.Closeables;

import com.xebialabs.overthere.OverthereProcess;

import static com.xebialabs.overthere.nio.process.Processes.execute;
import static com.xebialabs.overthere.nio.process.Processes.startProcess;
import static java.nio.file.Files.copy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class PlainNioScenario {
    public static final String GLASSFISH_ZIP = "glassfish-3.1.2.2.zip";
    public static final String PETCLINIC_EAR = "PetClinic-1.0.ear";
    public static final String GLASSFISH_DIR = "glassfish3";

    private FileSystem localFileSystem;
    protected FileSystem targetFileSystem;
    private Path unzipPath;
    private Path targetGlassfishDirPath;
    private Path localGlassfishZipPath;
    private Path localPetClinicEarPath;
    private Path targetGlassfishZipPath;
    private Path targetPetClinicDeployPath;
    private Process process;

    @BeforeClass
    public void setup() throws IOException {
        localFileSystem = FileSystems.getDefault();
        targetFileSystem = localFileSystem;

        localGlassfishZipPath = localFileSystem.getPath(GLASSFISH_ZIP);
        localPetClinicEarPath = localFileSystem.getPath(PETCLINIC_EAR);

        unzipPath = targetFileSystem.getPath("unzip");
        targetGlassfishZipPath = targetFileSystem.getPath("/tmp", GLASSFISH_ZIP);
        targetGlassfishDirPath = targetFileSystem.getPath("/tmp", GLASSFISH_DIR);
        targetPetClinicDeployPath = targetGlassfishDirPath.resolve("glassfish/domains/domain1/autodeploy").resolve(PETCLINIC_EAR);
    }

    @BeforeClass(dependsOnMethods = "setup")
    public void cleanup() throws IOException {
        if (Commons.isReachable("localhost", 4848, "/")) {
            execute(targetGlassfishDirPath.resolve("bin/asadmin"), "stop-domain");
        }

        if (Files.exists(targetGlassfishDirPath)) {
            Files.walkFileTree(targetGlassfishDirPath, new DeleteDirVisitor());
        }
        Files.deleteIfExists(targetGlassfishZipPath);
    }

    @Test
    public void copyGlassfishZip() throws IOException {
        // Copy GlassFish zip to remote machine.
        assertThat("Glassfish should not exist before copy", !Files.exists(targetGlassfishZipPath));
        copy(localGlassfishZipPath, targetGlassfishZipPath, StandardCopyOption.REPLACE_EXISTING);
        assertThat("Glassfish should exist after copy", Files.exists(targetGlassfishZipPath));
    }

    @Test(dependsOnMethods = "copyGlassfishZip")
    public void unzipGlassfishZip() throws IOException {
        // Unzip GlassFish zip on remote machine
        assertThat("Glassfish dir should not exist before unzip", !Files.exists(targetGlassfishDirPath));
        Process unzipProcess = new ProcessBuilder(unzipPath.toString(), targetGlassfishZipPath.toString(), "-d", targetGlassfishDirPath.getParent().toString()).redirectErrorStream(true).start();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(unzipProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } finally {
            unzipProcess.destroy();
        }
        assertThat("Glassfish dir should exist after unzip", Files.exists(targetGlassfishDirPath));
    }

    @Test(dependsOnMethods = "unzipGlassfishZip")
    public void startGlassfishContainer() throws IOException, InterruptedException {
        // Start GlassFish container
        assertThat("GlassFish should not be started yet", !Commons.isReachable("localhost", 4848, "/"));
        process = new ProcessBuilder(targetGlassfishDirPath.resolve("glassfish/bin/startserv").toString()).redirectErrorStream(true).start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        System.out.println("------------------------- Glassfish boot log -------------------------");
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
            if (line.contains("INFO: Successfully launched")) {
                break;
            }
        }
        System.out.println("----------------------------------------------------------------------");

        Commons.waitUntilReachable("localhost", 4848, "/");
    }

    @Test(dependsOnMethods = "startGlassfishContainer")
    public void deployPetClinicEar() throws IOException, InterruptedException {
        // Deploy PetClinic-1.0.ear on GlassFish.
        assertThat(Commons.getStatusCode(URI.create("http://localhost:8080/petclinic")), equalTo(404));
        copy(localPetClinicEarPath, targetPetClinicDeployPath, StandardCopyOption.REPLACE_EXISTING);
        Commons.waitUntilReachable("localhost", 8080, "/petclinic");
        assertThat(Commons.getStatusCode(URI.create("http://localhost:8080/petclinic")), equalTo(200));
    }

    @Test(dependsOnMethods = "deployPetClinicEar")
    public void runGlassfishScenario() throws IOException, InterruptedException {
        try {
            Commons.waitUntilButtonClicked();
        } finally {
            // Kill GlassFish process.
            process.destroy();
        }
        Files.walkFileTree(targetGlassfishDirPath, new DeleteDirVisitor());
        Files.deleteIfExists(targetGlassfishZipPath);
    }

}
