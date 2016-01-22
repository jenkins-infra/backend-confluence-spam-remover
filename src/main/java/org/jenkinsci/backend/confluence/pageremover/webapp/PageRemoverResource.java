/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.jenkinsci.backend.ldap.AccountServer;
import org.jenkinsci.backend.ldap.Config;
import org.kohsuke.stapler.config.ConfigurationLoader;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;

import static org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace;
import static org.jenkinsci.backend.confluence.pageremover.Spambot.BLACKLIST;

@Path("/page-remover")
@Produces(MediaType.TEXT_PLAIN)
public class PageRemoverResource {
    private final String space;
    private final String mailRecipient;
    private final String smtpServer;

    public PageRemoverResource(String space, String mailRecipient, String smtpServer) {
        this.space = space;
        this.mailRecipient = mailRecipient;
        this.smtpServer = smtpServer;
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

        System.err.println("Not a confluence notification email: " + subject.or("[empty subject]"));
        return Response.ok().build();

    }

    /**
     * Processes notification email from Confluence, and take appropriate actions.
     */
    private void processNotification(PageNotification n) throws Exception {
        System.err.println("Parsed "+n);

        if (n.action.equals("added")) {
            Connection con = new Connection();
            RemotePage p = con.getPage(n.spaceID, n.pageTitle);
            Language lang = null;
            try {
                lang = new LanguageDetection().detect(p.getContent());
                if (lang.lang.equals("id") && lang.prob>0.99) {
                    // highly confident that this is a spam. go ahead and remove it
                    removePage(con, p);
                    banAccount(n.authorID);
                    return;
                }
            } catch (LangDetectException e) {
                System.err.println("Failed to detect language");
                e.printStackTrace();
            }

            if (BLACKLIST.matches(p.getContent()) || BLACKLIST.matches(p.getTitle())) {
                removePage(con, p);
                banAccount(n.authorID);
                return;
            }

            String body = String.format("Language detection: %s\nWiki: %s\n\n\nSee https://github.com/jenkinsci/backend-confluence-spam-remover about this bot", lang, n);
            System.err.println(body);

            sendResponse(String.format("Keeping page %s", p.getTitle()), body);
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
    private void removePage(String subject, String content) throws Exception {
        for (String line : content.split("\n")) {
            if (line.equals("KILL SPAM")) {
                // instruction to remove
                Space sp = Space.find(subject);
                if (sp!=null) {
                    String pageTitle = subject.substring(sp.replySubjectPrefix.length());
                    System.err.println("Removing " + pageTitle);
                    Connection con = new Connection();
                    RemotePage pg = con.getPage(sp.id, pageTitle);
                    removePage(con, pg);
                    return;
                }
            }
        }
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

}
