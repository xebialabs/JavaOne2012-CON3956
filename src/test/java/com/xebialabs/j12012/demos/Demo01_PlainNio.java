package com.xebialabs.j12012.demos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import com.xebialabs.j12012.Commons;
import com.xebialabs.j12012.Scenario;

import static org.hamcrest.MatcherAssert.assertThat;

public class Demo01_PlainNio extends Scenario {
    private Process process;

    @Override
    protected void initTarget() throws IOException {
        targetFileSystem = FileSystems.getDefault();
        targetCopyPathName = "/tmp";
        unzipPathName = "unzip";
    }

    @Override
    protected String getIp() {
        return "localhost";
    }

    @Override
    public void unzipGlassfishZip() throws IOException, InterruptedException {
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
            unzipProcess.waitFor();
        }
        assertThat("Glassfish dir should exist after unzip", Files.exists(targetGlassfishDirPath));

    }

    @Override
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

    @Override
    public void stopGlassfishContainer() {
        process.destroy();
    }
}
