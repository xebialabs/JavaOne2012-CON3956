package com.xebialabs.j12012.demos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import com.xebialabs.j12012.Commons;
import com.xebialabs.j12012.Scenario;

import static java.lang.System.out;
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
    public void unzipGlassFishZip() throws IOException, InterruptedException {
        // Unzip GlassFish zip on remote machine
        out.println("*** Unzipping GlassFish zip");
        assertThat("GlassFish dir should not exist before unzip", !Files.exists(targetGlassFishDirPath));
        Process unzipProcess = new ProcessBuilder(unzipPath.toString(), targetGlassFishZipPath.toString(), "-d", targetGlassFishDirPath.getParent().toString()).redirectErrorStream(true).start();
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
        assertThat("GlassFish dir should exist after unzip", Files.exists(targetGlassFishDirPath));

    }

    @Override
    public void startGlassFishContainer() throws IOException, InterruptedException {
        out.println("*** Starting GlassFish container");

        assertThat("GlassFish should not be started yet", !Commons.isReachable(adminURI));
        process = new ProcessBuilder(targetGlassFishDirPath.resolve("glassfish/bin/startserv").toString()).redirectErrorStream(true).start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        System.out.println("------------------------- GlassFish boot log -------------------------");
        while ((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
            if (line.contains("INFO: Successfully launched")) {
                break;
            }
        }
        System.out.println("----------------------------------------------------------------------");

        Commons.waitUntilReachable(adminURI);
    }

    @Override
    public void stopGlassFishContainer() throws IOException, InterruptedException {
        out.println("*** Stopping GlassFish container");

        Process stopProcess = new ProcessBuilder(targetGlassFishDirPath.resolve("bin/asadmin").toString(), "stop-domain").redirectErrorStream(true).start();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(stopProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } finally {
            stopProcess.waitFor();
        }

        process.waitFor();
    }
}
