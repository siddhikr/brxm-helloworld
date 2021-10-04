package com.example.learning.brxm.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.hippoecm.repository.util.RepoUtils;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

// Helper util class for encoding, string sanitization etc
public class MyEncoderUtils {

    // Ideal scenario -there may be a separate HTMLSanitizer service to sanlitize input on the website page
    public static String sanitizeInputString(String input) {
        HtmlPolicyBuilder htmlPolicyBuilder = new HtmlPolicyBuilder();
        htmlPolicyBuilder.allowStandardUrlProtocols();
        htmlPolicyBuilder.allowUrlProtocols("data");
        htmlPolicyBuilder.allowStyling();
        htmlPolicyBuilder.disallowElements("script");
        PolicyFactory htmlPolicy = htmlPolicyBuilder.toFactory();
        return htmlPolicy.sanitize(input);
    }

    public static String encodeCharactersForJCRQueries(String input) {
        String output = null;
        if (StringUtils.isNotEmpty(input)) {
            output = Text.escapeIllegalXpathSearchChars(input).replaceAll("'", "''");
            output = output.replaceAll(":", "_x003A_");
            output = RepoUtils.encodeXpath(output);
        }
        return output;
    }
}