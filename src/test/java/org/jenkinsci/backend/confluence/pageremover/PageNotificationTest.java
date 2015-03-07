package org.jenkinsci.backend.confluence.pageremover;

import org.junit.Assert;
import org.junit.Test;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class PageNotificationTest extends Assert {
    @Test
    public void parse() throws Exception {
        PageNotification n = load("mail1.txt");
        assertEquals("Obat Penggugur Kandungan Ampuh 085727827778", n.pageTitle);
        assertEquals("obatfarmasi", n.authorID);
        assertEquals("added", n.action);

        n = load("mail2.txt");
        assertEquals("OWASP Dependency-Check Plugin", n.pageTitle);
        assertEquals("sspringett", n.authorID);
        assertEquals("edited", n.action);

        n = load("mail3.txt");
        assertEquals("Pretested Integration Plugin", n.pageTitle);
        assertEquals("bue", n.authorID);
        assertEquals("edited", n.action);
    }

    private PageNotification load(String name) throws Exception {
        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()),
                getClass().getClassLoader().getResourceAsStream("notifications/" + name));
        return PageNotification.parse(msg);
    }

}