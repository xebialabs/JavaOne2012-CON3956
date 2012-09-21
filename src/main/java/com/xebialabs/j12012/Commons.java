package com.xebialabs.j12012;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import javax.swing.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;

public class Commons {

    public static boolean isReachable(String ip, int port, String resource) throws IOException {
        int statusCode = getStatusCode(URI.create("http://" + ip + ":" + port + resource));
        return statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED;
    }

    public static int getStatusCode(URI uri) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri);
        try {
            HttpResponse execute = httpClient.execute(httpGet);
            int statusCode = execute.getStatusLine().getStatusCode();
            return statusCode;
        } catch (HttpHostConnectException hhce) {
            return 500;
        }
    }

    public static void waitUntilButtonClicked() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        JFrame jFrame = new JFrame();
        JButton jButton = new JButton("Click to terminate");
        jFrame.add(jButton);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                countDownLatch.countDown();
            }
        });
        jFrame.setTitle("Test");
        jFrame.setSize(200, 100);
        jFrame.setVisible(true);
        countDownLatch.await();
    }

    public static void waitUntilReachable(final String ip, final int i, final String resource) throws InterruptedException, IOException {
        while (!isReachable(ip, i, resource)) {
            Thread.sleep(1000);
        }
    }


}
