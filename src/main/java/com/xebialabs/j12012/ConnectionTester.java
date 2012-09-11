package com.xebialabs.j12012;

import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;

public class ConnectionTester {

    public static boolean isReachable(String ip, int port) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(URI.create("http://" + ip + ":" + port));
        try {
            HttpResponse execute = httpClient.execute(httpGet);
            int statusCode = execute.getStatusLine().getStatusCode();
            return statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED;
        } catch (HttpHostConnectException hhce) {
            return false;
        }
    }
}
