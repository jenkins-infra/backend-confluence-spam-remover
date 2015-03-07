package org.jenkinsci.backend.confluence.pageremover;

import hudson.plugins.jira.soap.ConfluenceSoapService;
import org.jvnet.hudson.confluence.Confluence;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Connection to confluence.
 *
 * @author Kohsuke Kawaguchi
 */
public class Connection {
    public final ConfluenceSoapService service;
    public final String token;

    public Connection() throws IOException, ServiceException {
        service = Confluence.connect(new URL("https://wiki.jenkins-ci.org/"));

        Properties props = new Properties();
        File credential = new File(new File(System.getProperty("user.home")), ".jenkins-ci.org");
        if (!credential.exists())
            throw new IOException("You need to have userName and password in "+credential);
        props.load(new FileInputStream(credential));
        token = service.login(props.getProperty("userName"), props.getProperty("password"));
    }
}
