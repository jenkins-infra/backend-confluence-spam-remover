package org.jenkinsci.backend.confluence.pageremover;

import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import hudson.plugins.jira.soap.RemotePage;
import org.jenkinsci.backend.ldap.AccountServer;
import org.jenkinsci.backend.ldap.Config;
import org.kohsuke.stapler.config.ConfigurationLoader;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
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

        if (msg.getSubject().startsWith("Re: ")) {
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
            Connection con = new Connection();
            RemotePage p = con.getPage(n.spaceID, n.pageTitle);
            Language lang = null;
            try {
                lang = new LanguageDetection().detect(p.getContent());
                if (lang.lang.equals("id") && lang.prob>0.99) {
                    // highly confident that this is a spam. go ahead and remove it
                    removePage(msg, con, p);
                    banAccount(n.authorID);
                    return;
                }
            } catch (LangDetectException e) {
                System.err.println("Failed to detect language");
                e.printStackTrace();
            }

            if (BLACKLIST.matches(p.getContent()) || BLACKLIST.matches(p.getTitle())) {
                removePage(msg, con, p);
                banAccount(n.authorID);
                return;
            }

            String body = String.format("Keeping this page\n\nLanguage detection: %s\nWiki: %s\n\n\nSee https://github.com/jenkinsci/backend-confluence-spam-remover about this bot", lang, n);
            System.err.println(body);

            Transport.send(createResponse(msg, body));
        }
    }

    /**
     * Ban the user.
     */
    private void banAccount(String id) {
        try {
            AccountServer app = new AccountServer(ConfigurationLoader.from(new File("./config.properties")).as(Config.class));
            app.delete(id);
            System.err.println("Successfully deleted account "+id);
        } catch (Exception e) {
            System.err.println("Failed to delete account "+id);
            e.printStackTrace();
        }
    }

    /**
     * If a human replies to a notification email, take action.
     */
    private void removePage(MimeMessage reply) throws Exception {
        String content = getContent(reply);
        for (String line : content.split("\n")) {
            if (line.equals("KILL SPAM")) {
                // instruction to remove
                Space sp = Space.find(reply);
                if (sp!=null) {
                    String pageTitle = reply.getSubject().substring(sp.replySubjectPrefix.length());
                    System.err.println("Removing " + pageTitle);
                    Connection con = new Connection();
                    RemotePage pg = con.getPage(sp.id, pageTitle);
                    removePage(reply, con, pg);
                    return;
                }
            }
        }
    }

    private void removePage(MimeMessage current, Connection con, RemotePage pg) throws MessagingException {
        try {
            con.removePage(pg.getId());

            Transport.send(createResponse(current,
                    String.format("Removed page: %s\n", pg.getTitle())));
        } catch (Exception e) {
            Transport.send(createResponse(current,
                    String.format("Failed to delete page: %s\n%s", pg.getTitle(), print(e))));
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

    public static final WordList BLACKLIST = new WordList();

    static {
        try {
            BLACKLIST.load(Spambot.class.getClassLoader().getResource("blacklist.wl"));
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
