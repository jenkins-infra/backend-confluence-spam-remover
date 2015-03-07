package org.jenkinsci.backend.confluence.pageremover;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import org.apache.commons.io.IOUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class LanguageDetection {
    public Language detect(String text) throws LangDetectException {
        Detector d = DetectorFactory.create();
        d.append(text);
        ArrayList<Language> r = d.getProbabilities();
        if (r.isEmpty())    return null;
        return r.get(0);
    }

    // load language detection profiles
    static {
        try {
            List<String> profiles = new ArrayList<String>();
            for (String lang : Arrays.asList("en", "id", "ja")) {
                profiles.add(IOUtils.toString(EmailProcessor.class.getResourceAsStream("/profiles/" + lang)));
            }
            DetectorFactory.loadProfile(profiles);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
