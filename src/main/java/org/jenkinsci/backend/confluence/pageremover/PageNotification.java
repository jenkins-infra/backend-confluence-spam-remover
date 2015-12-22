package org.jenkinsci.backend.confluence.pageremover;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsed page notification email.
 *
 * @author Kohsuke Kawaguchi
 */
public class PageNotification {
    /**
     * "added", "edited", etc.
     */
    public final String action;

    /**
     * Page title, such as "Pretested Integration Plugin"
     */
    public final String pageTitle;

    /**
     * Author ID of the page, such as "kohsuke" or "autojack"
     */
    public final String authorID;

    /**
     * Space ID such as "JENKINS" or "JA"
     */
    public final String spaceID;

    public PageNotification(String action, String pageTitle, String authorID, String spaceID) {
        this.action = action;
        this.pageTitle = pageTitle;
        this.authorID = authorID;
        this.spaceID = spaceID;
    }

    @Override
    public String toString() {
        return "PageNotification{" +
                "action='" + action + '\'' +
                ", pageTitle='" + pageTitle + '\'' +
                ", authorID='" + authorID + '\'' +
                ", spaceID='" + spaceID + '\'' +
                '}';
    }

    /**
     * Parse the email as confluence notification.
     * If the email doesn't match, return null.
     */
    public static PageNotification parse(MimeMessage msg) throws MessagingException, IOException {
        Space sp=Space.find(msg);
        if (sp==null)
            return null;

        String pageTitle = msg.getSubject().substring(sp.subjectPrefix.length());

        List<String> contents = Arrays.asList(msg.getContent().toString().split("\n"));
        if (contents.size()<10)     return null;    // too short to be real

        /*
            The portion of the email body we are looking for is this:

<div class="email">
    <h2><a href="https://wiki.jenkins-ci.org/display/JENKINS/Obat+Penggugur+Kandungan+Ampuh+085727827778">Obat Penggugur Kandungan Ampuh 085727827778</a></h2>
    <h4>Page  <b>added</b> by             <a href="https://wiki.jenkins-ci.org/display/~obatfarmasi">gilang kurniawan</a>

         */
        for (int i=0; i<contents.size(); i++) {
            if (contents.get(i).trim().equals("<div class=\"email\">")) {
                if (i+2>=contents.size())   return null;    // expecting two more lines to follow

                String line2 = contents.get(i + 2);
                Matcher m = ACTION_PATTERN.matcher(line2);
                if (!m.find())              return null;    // expecting to find action
                String action = m.group(1);

                m = USERID_PATTERN.matcher(line2);
                if (!m.find())              return null;    // expecting to find user ID
                String userId = m.group(1);

                return new PageNotification(action,pageTitle,userId,sp.id);
            }
        }

        return null;
    }

    // find the action verb
    private static final Pattern ACTION_PATTERN = Pattern.compile("<b>([a-z]+)</b>");

    // find the user ID
    private static final Pattern USERID_PATTERN = Pattern.compile("https://wiki.jenkins-ci.org/display/~([^\"]+)\">");
}
