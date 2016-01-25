package org.jenkinsci.backend.confluence.pageremover;

import com.sun.webkit.network.URLs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public class WordList {
    private final Set<String> words = new HashSet<String>();

    public WordList() {
    }

    /**
     * Loads a word list into this.
     */
    public void load(URL res) throws IOException {

        BufferedReader r = new BufferedReader(new InputStreamReader(res.openStream(),"UTF-8"));
        r.lines().filter(line -> !line.startsWith("#"))
                .forEach(line ->
                    words.add(line.trim())
                );
    }

    /**
     * Checks if the given word is in the set.
     */
    public boolean isIn(String word) {
        return words.contains(word);
    }

    /**
     * Checks if the word appears in the given text. This is without any fancy tokenizations.
     */
    public boolean matches(final String longText) {
        if (words.stream()
                .filter(word -> longText.toLowerCase(Locale.ENGLISH).contains(word))
                .findFirst()
                .isPresent()) {
            return true;
        }

        return Pattern.compile("8(?:00|44|55|66|77|88)[ ~_\\-.=)]*\\d{3}[ ~_.\\-=]*\\d{4}").matcher(longText).matches();
    }
}
