package org.jenkinsci.backend.confluence.pageremover;

import com.cybozu.labs.langdetect.Language;
import hudson.plugins.jira.soap.RemotePage;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Entry point for the app that responds to confluence notification emails
 *
 * @author Kohsuke Kawaguchi
 */
public class Spambot {

    public static void main(String[] args) throws Exception {
        System.exit(new Spambot().run());
    }

    /**
     * Reads an email (to jenkinsci-spambot@googlegroups.com) from stdin, and take actions.
     */
    public int run() throws Exception {
        // we get email in stdin
        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(System.getProperties()), System.in);

        if (msg.getSubject().startsWith(REPLY_SUBJECT_PREFIX)) {
            removePage(msg);
            return 0;
        }

        PageNotification n = PageNotification.parse(msg);
        if (n!=null) {
            processNotification(msg, n);
            return 0;
        }

        System.err.println("Not a confluence notification email: "+msg.getSubject());
        return 1;
    }

    /**
     * Processes notification email from Confluence, and take appropriate actions.
     */
    private void processNotification(MimeMessage msg, PageNotification n) throws Exception {
        System.err.println("Parsed "+n);

        if (n.action.equals("added")) {
            RemotePage p = new Connection().getPage("JENKINS", n.pageTitle);
            Language lang = new LanguageDetection().detect(p.getContent());

            String body = String.format("Language detection: %s\nWiki: %s\n\n\nSee https://github.com/jenkinsci/backend-confluence-spam-remover about this bot", lang, n);
            System.err.println(body);

            Transport.send(createResponse(msg, body));
        }
    }

    /**
     * If a human replies to a notification email, take action.
     */
    private void removePage(MimeMessage reply) throws Exception {
        String content = getContent(reply);
        if (content.contains("\nKILL SPAM\n")) {
            // instruction to remove
            String pageTitle = reply.getSubject().substring(REPLY_SUBJECT_PREFIX.length());
            System.err.println("Removing "+pageTitle);
            try {
                Connection con = new Connection();
                RemotePage pg = con.getPage("JENKINS", pageTitle);
                con.removePage(pg.getId());

                Transport.send(createResponse(reply,
                        String.format("Removed page: %s\n", pageTitle)));
            } catch (Exception e) {
                Transport.send(createResponse(reply,
                        String.format("Failed to delete page: %s\n%s", pageTitle, print(e))));
            }
        }
    }

    private String getContent(MimeMessage reply) throws IOException, MessagingException {
        Object c = reply.getContent();
        if (c instanceof MimeMultipart) {
            c = ((MimeMultipart) c).getBodyPart(0).getContent();
        }
        return c.toString();
    }

    /**
     * Prints a {@link Throwable}.
     */
    private String print(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Creates a response message from the given plain text body,
     */
    private Message createResponse(MimeMessage msg, String body) throws MessagingException {
        Message reply = msg.reply(false);
        reply.setFrom(new InternetAddress("spambot@infradna.com"));
        reply.setContent(body,"text/plain");
        return reply;
    }

    private static final String REPLY_SUBJECT_PREFIX = "Re: [confluence] Jenkins > ";
}
