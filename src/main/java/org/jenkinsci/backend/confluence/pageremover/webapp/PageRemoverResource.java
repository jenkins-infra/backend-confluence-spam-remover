package org.jenkinsci.backend.confluence.pageremover.webapp;

import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.google.common.base.Optional;
import hudson.plugins.jira.soap.RemotePage;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.jenkinsci.backend.confluence.pageremover.Connection;
import org.jenkinsci.backend.confluence.pageremover.LanguageDetection;
import org.jenkinsci.backend.confluence.pageremover.PageNotification;
import org.jenkinsci.backend.confluence.pageremover.Space;
import org.jenkinsci.backend.confluence.pageremover.Spambot;
import org.jenkinsci.backend.confluence.pageremover.WordList;
import org.jenkinsci.backend.ldap.AccountServer;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace;
import static org.jenkinsci.backend.confluence.pageremover.Spambot.BLACKLIST;

@Path("/page-remover")
@Produces(MediaType.TEXT_PLAIN)
public class PageRemoverResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageRemoverResource.class);

    private final String mailRecipient;
    private final String smtpServer;
    private final AccountServer accountServer;
    private final List<String> languagesToBlock;
    private final float blockingProbability;


    public PageRemoverResource(String mailRecipient, String smtpServer,
                               AccountServer accountServer, List<String> languagesToBlock,
                               float blockingProbability) {
        this.mailRecipient = mailRecipient;
        this.smtpServer = smtpServer;
        this.accountServer = accountServer;
        this.languagesToBlock = languagesToBlock;
        this.blockingProbability = blockingProbability;
    }

    @POST
    public Response submit(@QueryParam("subject") Optional<String> subject,
                           @QueryParam("body-plain") Optional<String> body) throws Exception {

        PageNotification pageNotification = PageNotification.parse(subject.or(""), body.or(""));

        if (subject.or("").startsWith("Re: ") && body.isPresent()) {
            removePage(subject.get(), body.get());
            return Response.ok().build();
        }

        if (pageNotification != null) {
            processNotification(pageNotification);
            return Response.ok().build();
        }

        LOGGER.warn("Not a confluence notification email: " + subject.or("[empty subject]"));
        return Response.ok().build();

    }

    /**
     * Processes notification email from Confluence, and take appropriate actions.
     */
    private void processNotification(PageNotification n) throws Exception {
        LOGGER.info("Parsed " + n);

        if (n.action.equals("added")) {
            Connection con = new Connection();
            RemotePage p = con.getPage(n.spaceID, n.pageTitle);
            Language lang = null;
            try {
                lang = new LanguageDetection().detect(p.getContent());
                if (lang.prob > blockingProbability) {
                    if (languagesToBlock.contains(lang.lang)) {
                        LOGGER.info("Page in blocked language " + lang.lang + " detected - removing and banning account");

                        // highly confident that this is a spam. go ahead and remove it
                        removePage(con, p);
                        banAccount(n.authorID);
                        return;
                    }
                }
            } catch (LangDetectException e) {
                LOGGER.error("Failed to detect language", e);
            }

            if (BLACKLIST.matches(p.getContent()) || BLACKLIST.matches(p.getTitle())) {
                removePage(con, p);
                banAccount(n.authorID);
                return;
            }

            String body = String.format("Language detection: %s\nWiki: %s\n\n\nSee https://github.com/jenkinsci/backend-confluence-spam-remover about this bot", lang, n);
            LOGGER.info(body);

            sendResponse(String.format("Keeping page %s", p.getTitle()), body);
        }
    }

    /**
     * Ban the user.
     */
    private void banAccount(String id) {
        try {
            accountServer.delete(id);
            LOGGER.info("Successfully deleted account "+id);
        } catch (Exception e) {
            LOGGER.error("Failed to delete account " + id, e);
        }
    }

    /**
     * If a human replies to a notification email, take action.
     */
    private void removePage(String subject, String content) throws Exception {
        Stream.of(content.split("\n"))
                .filter(line -> line.equals("KILL SPAM"))
                .forEach(Unchecked.consumer(line -> {
                    // instruction to remove
                    Space sp = Space.find(subject);
                    if (sp != null) {
                        String pageTitle = subject.substring(sp.replySubjectPrefix.length());
                        LOGGER.info("Removing " + pageTitle);
                        Connection con = new Connection();
                        RemotePage pg = con.getPage(sp.id, pageTitle);
                        removePage(con, pg);
                    }
                }));
    }

    private void removePage(Connection con, RemotePage pg) throws EmailException {
        try {
            con.removePage(pg.getId());

            sendResponse(String.format("Removed page: %s\n", pg.getTitle()));
        } catch (Exception e) {
            sendResponse(String.format("Failed to delete page: %s\n%s", pg.getTitle()), getFullStackTrace(e));
        }
    }

    /**
     * Sends a response message from the given plain text subject with an empty body.
     */
    private void sendResponse(String subject) throws EmailException {
        sendResponse(subject, "");
    }

    /**
     * Sends a response message from the given plain text body and subject.
     */
    private void sendResponse(String subject, String body) throws EmailException {
        Email msg = new SimpleEmail();
        msg.setHostName(smtpServer);
        msg.setFrom("spambot@infradna.com");
        msg.addTo(mailRecipient);
        msg.setSubject(subject);
        msg.setMsg(String.format("%s\n%s", subject, body));
        msg.send();
    }

    public static final WordList BLACKLIST = new WordList();

    static {
        try {
            BLACKLIST.load(PageRemoverResource.class.getClassLoader().getResource("blacklist.wl"));
        } catch (IOException e) {
            throw new Error(e);
        }
    }


}
