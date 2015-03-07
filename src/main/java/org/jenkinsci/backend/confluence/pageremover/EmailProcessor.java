package org.jenkinsci.backend.confluence.pageremover;

import com.cybozu.labs.langdetect.Language;
import hudson.plugins.jira.soap.RemotePage;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

/**
 * Entry point for the app that responds to confluence notification emails
 *
 * @author Kohsuke Kawaguchi
 */
public class EmailProcessor {
    public static void main(String[] args) throws Exception {
        // we get email in stdin
        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()), System.in);
        PageNotification n = PageNotification.parse(msg);
        if (n==null) {
            System.err.println("Not a confluence notification email: "+msg.getSubject());
            return;
        }

        if (n.action.equals("added")) {
            RemotePage p = new Connection().getPage("JENKINS", n.pageTitle);
            Language lang = new LanguageDetection().detect(p.getContent());

            Message reply = msg.reply(false);
            reply.setContent("Language detection: "+lang.toString(),"text/plain");
            Transport.send(reply);
        }
    }
}
