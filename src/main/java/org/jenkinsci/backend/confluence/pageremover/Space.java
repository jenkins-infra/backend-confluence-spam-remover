package org.jenkinsci.backend.confluence.pageremover;

import org.jooq.lambda.Unchecked;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.stream.Stream;

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

    /**
     * Finds the space from a notification email.
     *
     * @return null if no space matches
     */
    public static Space find(MimeMessage msg) throws MessagingException {
        return Stream.of(SPACES).filter(Unchecked.predicate(s ->
                msg.getSubject().startsWith(s.subjectPrefix)
                        || msg.getSubject().startsWith(s.replySubjectPrefix)))
                .findFirst().orElse(null);
    }

    /**
     * Finds the space from a mail subject.
     *
     * @return null if no space matches
     */
    public static Space find(String subject) {
        return Stream.of(SPACES).filter(s ->
            subject.startsWith(s.subjectPrefix)
                    ||  subject.startsWith(s.replySubjectPrefix))
                .findFirst().orElse(null);
    }
}
