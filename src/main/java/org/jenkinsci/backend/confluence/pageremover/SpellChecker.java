package org.jenkinsci.backend.confluence.pageremover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Kohsuke Kawaguchi
 */
public class SpellChecker {
    private final Set<String> words = new HashSet<String>();

    public SpellChecker() {
        try {
            for (String name : new String[]{"en-common.wl","en-variant_0.wl","en-variant_1.wl","en-variant_2.wl","jenkins.wl"}) {
                BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(name)));
                try {
                    String line;

                    while ((line=r.readLine())!=null) {
                        words.add(line.trim().toLowerCase(Locale.ENGLISH));
                    }
                } finally {
                    r.close();
                }
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public float errorRateOf(String text) {
        int err=0,total=0;
        // ' as a separator doesn't work well because it splits words like "doesn't"
        StringTokenizer tokens = new StringTokenizer(text," \t\n\r\f(){}<>[]|:#,.=\"");
        while (tokens.hasMoreTokens()) {
            String t = tokens.nextToken();
            t = undecorate(t);

            total++;
            if (isValidWord(t))
                continue;
            err++;
//            System.out.println(t);
        }

        return err*100.0f/total;
    }

    /**
     * Remove textual decoration.
     */
    private String undecorate(String t) {
        // trim off decorators from head and tail
        while (t.length()>0) {
            if (isDecraotr(t.charAt(0)))
                t = t.substring(1);
            else
                break;
        }
        while (t.length()>0) {
            if (isDecraotr(t.charAt(t.length()-1)))
                t = t.substring(0,t.length()-1);
            else
                break;
        }

        if (t.length()==0)  return "harmless";

        return t;
    }

    private boolean isDecraotr(char c) {
        return DECORATOR.indexOf(c)>=0;
    }

    private static final String DECORATOR = "*-_\"'?!";

    private boolean isValidWord(String t) {
        if (words.contains(t.toLowerCase(Locale.ENGLISH)))
            return true;

        try {// number?
            Float.parseFloat(t);
            return true;
        } catch (NumberFormatException e) {
        }

        return false;
    }
}
