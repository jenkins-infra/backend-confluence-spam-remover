package org.jenkinsci.backend.confluence.pageremover;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
public class ConfluenceMarkupNormalizerTest extends Assert {
    ConfluenceMarkupNormalizer n = new ConfluenceMarkupNormalizer();
    @Test
    public void testLinkReplace() {
        assertEquals("foo bar text", n.translate("foo [bar|zot] text"));
    }
}
