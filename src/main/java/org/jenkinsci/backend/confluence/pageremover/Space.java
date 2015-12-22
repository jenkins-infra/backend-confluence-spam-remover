package org.jenkinsci.backend.confluence.pageremover;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * Known Wiki spaces, such as JENKINS, JA, and INFRA.
 * @author Kohsuke Kawaguchi
 */
public class Space {
    public final String id;

    /**
     * Prefix of the email notification.
     */
    public final String subjectPrefix;

    /**
     * Prefix a reply to the email notification.
     */
    public final String replySubjectPrefix;

    public Space(String id, String prefix) {
        this.id = id;
        this.subjectPrefix = prefix;
        this.replySubjectPrefix= "Re: "+prefix;
    }

    private static final Space[] SPACES = new Space[] {
            new Space("JENKINS",    "[confluence] Jenkins > "),
            new Space("JA",         "[confluence] 日本語 > ")
    };

    public static final Space find(MimeMessage msg) throws MessagingException {
        for (Space s : SPACES) {
            if (msg.getSubject().startsWith(s.subjectPrefix)
            ||  msg.getSubject().startsWith(s.replySubjectPrefix))
                return s;
        }
        return null;
    }
}
