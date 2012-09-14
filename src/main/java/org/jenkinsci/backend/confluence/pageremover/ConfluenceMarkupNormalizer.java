package org.jenkinsci.backend.confluence.pageremover;

import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public class ConfluenceMarkupNormalizer {
    public String translate(String text) {
        // confluence macros
        text = text.replaceAll("&nbsp;"," ");
        text = text.replaceAll("\\\\"," ");

        // ignore hyperlink URLs
        text = LINK.matcher(text).replaceAll("$1");

        // quoted text is mostly source code and stack trace
        text = NOFORMAT.matcher(text).replaceAll("");

        // quash any macro
        text = MACRO.matcher(text).replaceAll("");

        return text;
    }

    private static final Pattern LINK = Pattern.compile("\\[([^|\\]]+)\\|([^|\\]]+)\\]");
    private static final Pattern NOFORMAT = Pattern.compile("\\{noformat\\}.+?\\{noformat\\}",Pattern.MULTILINE|Pattern.DOTALL);
    private static final Pattern MACRO = Pattern.compile("\\{[a-z-]+(:[^}]+)?\\}");
}
