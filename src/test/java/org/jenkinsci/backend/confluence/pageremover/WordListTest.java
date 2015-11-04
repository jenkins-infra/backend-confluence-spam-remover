package org.jenkinsci.backend.confluence.pageremover;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class WordListTest extends Assert {
    @Test
    public void sanity() throws Exception {
        WordList wl = new WordList();
        wl.load(Spambot.class.getClassLoader().getResource("blacklist.wl"));
        assertTrue(wl.isIn("solahartcenter.com"));
        assertTrue(!wl.isIn("kohsuke.org"));
        assertTrue(wl.matches("http://solahartcenter.com"));
    }
}
