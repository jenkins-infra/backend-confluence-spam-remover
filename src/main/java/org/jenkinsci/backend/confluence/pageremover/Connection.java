package org.jenkinsci.backend.confluence.pageremover;

import hudson.plugins.jira.soap.ConfluenceSoapService;
import hudson.plugins.jira.soap.RemotePage;
import hudson.plugins.jira.soap.RemotePageSummary;
import hudson.plugins.jira.soap.RemoteUserInformation;
import org.jvnet.hudson.confluence.Confluence;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
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
        File credential = new File(new File(System.getProperty("user.home")), ".jenkins-ci.org.spambot");
        if (!credential.exists())
            credential = new File(new File(System.getProperty("user.home")), ".jenkins-ci.org");
        if (!credential.exists())
            throw new IOException("You need to have userName and password in "+credential);
        props.load(new FileInputStream(credential));
        token = service.login(props.getProperty("userName"), props.getProperty("password"));
    }

    public RemotePageSummary[] getPages(String space) throws RemoteException {
        return service.getPages(token,space);
    }

    public RemotePage getPage(long pageId) throws RemoteException {
        return service.getPage(token,pageId);
    }

    public RemotePage getPage(String space, String title) throws RemoteException {
        return service.getPage(token,space,title);
    }

    public RemoteUserInformation getUserInformation(String userId) throws RemoteException {
        return service.getUserInformation(token,userId);
    }

    public void removePage(long pageId) throws RemoteException {
        service.removePage(token,pageId);
    }
}
