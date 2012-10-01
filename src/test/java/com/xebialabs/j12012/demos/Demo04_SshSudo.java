package com.xebialabs.j12012.demos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.io.CharStreams;
import com.google.common.io.OutputSupplier;

import com.xebialabs.j12012.OverthereScenarioWithOvercast;

import static com.google.common.collect.Maps.newHashMap;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PRIVATE_KEY_FILE;

public class Demo04_SshSudo extends OverthereScenarioWithOvercast {
    @Override
    protected void initTarget() throws IOException {
        setupHost("glassfish_ssh");
        final Map<String, String> options = newHashMap();
        options.put(PRIVATE_KEY_FILE, createPrivateKeyFile(
                  "-----BEGIN RSA PRIVATE KEY-----\r\n"
                + "MIIEpgIBAAKCAQEA65Jf19SCv8rZ/kLyfOw+OjHt5fQnxHVQR2B6UW0B0q6RhSSg\r\n"
                + "YA2gOUEPJph7+O5605jjrMlblScsXO7VnFJtBTMRFoQjBlOP8VFuEg0MoaN5RPQU\r\n"
                + "ZVYoFKP9V6ycEKpmfeSmVhpEipUdgL+fUjb1nsw+Cpx8WNLXf3Cvqrk4d1xZ0f0i\r\n"
                + "u3D4/B+LmyZuVqvlmRfVDOe0Orvo/hfkiM+hMqKnFfKnUldvUb6feq4HrxpeYoEm\r\n"
                + "sO+oqkNfVp/RfXiuSzBHQl6sYYwWDSHgsyIo/7o2ATcDw3FzErxR3pMjH0rqh39A\r\n"
                + "TJtFTzvV90VNTRd+1727hzS6IQLf+M/Y6OvzawIDAQABAoIBAQDashc8XdPMjlv2\r\n"
                + "ytwn0YKrsDK1qwdIQcj3mr+z3Ek2+E2sl6YzxjKbNKUGJcXiAjQRQP0NKhpVy/pJ\r\n"
                + "hIjXCUag7xnMF3wUoXseg4R2SZsSbJtmwlo1AdlP4DaQMHTqm+dutNkfUl+TcH/l\r\n"
                + "SQB16QP6Go72dvSR2Zuqekj7a9zaISAopv+B68ShkX5NIf/96JCLpa0J7O00dN6k\r\n"
                + "bO9UiH3g6jtb9CgmG836rXabdTpSma1fsOLJCq/sEkCL1ImF1rCKTIjvK0djDdjK\r\n"
                + "NEL7e0v4s/RYqCIiCoji617Xesu9OYgYHWBkH3mSRKzIw6LUSxAQKiiCwTDaYNz5\r\n"
                + "X4IFy8ihAoGBAP1i4ztQpBwq4teEHuJ6klpFyT03tcMqFxK1zrcr0qQSqhc8ww55\r\n"
                + "AWUKACS7QAA0hFH286+dzYApo0jn2MRzIu9KP9xUEX4K0qw3h/zsBxkIGsUbUSp7\r\n"
                + "7f5mK8vfTJstUZbyh/XlJDWNI5Q/QvybgAhZmXYoQHt6r4NwcOj47A9JAoGBAO4A\r\n"
                + "cb//EB+Nj62kKQJN/UI60VZ5Koo3cBXwLhkVsfCfUXwOEb9uhYs3bePGRadgpn3z\r\n"
                + "6XvYs0IU4mFabvAMXLQz59zVFqotEEsCVy1acETbaCYCHLRRolxsdMeF528HEMqB\r\n"
                + "7tTJVbrmZaWxtnHzd3Mlj9tiszrwvidQUJ33s0kTAoGBAPpGHmOL90zLH1v35/mT\r\n"
                + "T9NScr7AtAudG0UjxpYt9tSQiuiA37j/1FzUT+f3+/M37Cp5XaDsoPoiJmHwfq8r\r\n"
                + "eioYkJMzhkOUtRndj7hF+YzD8I0XukfYOO66RDAO0z/Ct3/89kXumqE6UxYulh+k\r\n"
                + "CAY3WdjXUTmlqI6PFTdIBwHhAoGBAKKWtR6nfXlAuO2znrxPUPtEuSus3K3Nj4m9\r\n"
                + "KZDDbGroO79W0TMIqrxfYnffREhC05pp3ZBYiqVTJQ/CutTMbSxB5VzMSY55+I51\r\n"
                + "i96U0OuJQ83rVXat6g/fm6uOQ3tqxULCnsjIvgNPUBNwoyWXYHvOJkeGVtCmFBFB\r\n"
                + "YcF4rQb3AoGBAJ7Ekj7G0qDD+gJQ3JnWfWJNHHDU1LclntuZlzrLhKEAku9y7Q5I\r\n"
                + "tygMnR1DYrWKTZPDtrCb1NJqPntPp8A4VMnkZ89AY11CexWrtDx9VDy4MJFr8uCx\r\n"
                + "c05G9exoAKHonnFcWxOg+xIgofdfic3/vTg/aGPp1I2urqLzv00vPyeX\r\n"
                + "-----END RSA PRIVATE KEY-----\r\n").getPath());
        options.put("sudoUsername", "overthere");
  
        
        targetFileSystem = FileSystems.newFileSystem(URI.create("ssh+sudo://trusted@" + host.getHostName() + "/?os=UNIX"), options);
        targetCopyPathName = "/tmp";
        unzipPathName = "unzip";
    }

    private static File createPrivateKeyFile(String privateKey) throws IOException {
        final File privateKeyFile = File.createTempFile("private", ".key");
        privateKeyFile.deleteOnExit();
        CharStreams.write(privateKey, new OutputSupplier<Writer>() {
            @Override
            public Writer getOutput() throws IOException {
                return new FileWriter(privateKeyFile);
            }
        });
        return privateKeyFile;
    }

    @Test
    public void neededForTestNGPluginForEclipseToRecognizeThisClassAsATest() { }

}
