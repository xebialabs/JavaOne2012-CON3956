package com.xebialabs.j12012;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Commons {

    public static boolean isReachable(URI uri) throws IOException {
        int statusCode = getStatusCode(uri);
        return statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED;
    }

    public static int getStatusCode(URI uri) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(uri);
        try {
            HttpResponse execute = httpClient.execute(httpGet);
            int statusCode = execute.getStatusLine().getStatusCode();
            logger.info("URI <{}> --> {}", uri, statusCode);
            return statusCode;
        } catch (HttpHostConnectException hhce) {
            return 500;
        }
    }

    public static void waitUntilButtonClicked(String url) throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        JFrame jFrame = new JFrame();
        addLink(jFrame, url);
        JButton jButton = new JButton("Click to terminate");
        jFrame.add(jButton);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                countDownLatch.countDown();
            }
        });
        jFrame.setTitle("Test");
        jFrame.setSize(500, 50);
        jFrame.setVisible(true);
        countDownLatch.await();
    }

    private static void addLink(final JFrame jFrame, final String url) {
        jFrame.setLayout(new GridBagLayout());
        JButton button = new JButton("<html><font style='color: #009;'><u>" + url + "</u></font></html>");
        Container contentPane = jFrame.getContentPane();
        contentPane.add(button);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(URI.create(url));
                    } catch (IOException ioe) { /* TODO: error handling */ }
                } else { /* TODO: error handling */ }
            }
        });
    }

    public static void waitUntilReachable(URI uri) throws InterruptedException, IOException {
        logger.info("Waiting until <{}> is reachable", uri);
        while (!isReachable(uri)) {
            Thread.sleep(1000);
        }
    }

    public static void dumpStream(final InputStream from, final OutputStream to) {
        Thread dumper = new Thread(new Runnable() {
            public void run() {
                try {
                    int cInt = from.read();
                    while(cInt != -1) {
                        to.write(cInt);
                        cInt = from.read();
                    }
                } catch(IOException ignore) { }
            }
        });
        dumper.start();
    }


    private static final Logger logger = LoggerFactory.getLogger(Commons.class);
}
