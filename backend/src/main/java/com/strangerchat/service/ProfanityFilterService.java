package com.strangerchat.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ProfanityFilterService {

    // Common profanity and harassment keywords list (expandable)
    private static final Set<String> PROFANITY_WORDS = new HashSet<>(Arrays.asList(
            "badword1", "badword2", "nigger", "faggot", "retard", "cunt", "whore", "slut",
            "kill yourself", "kys", "chink", "spic"
    ));

    private static final Pattern SPAM_REPEATED_CHARS = Pattern.compile("(.)\\1{7,}");

    public String sanitizeAndFilter(String rawText) {
        if (rawText == null) {
            return "";
        }

        // 1. Trim and collapse excessive repetitive whitespace
        String cleaned = rawText.trim().replaceAll("\\s+", " ");

        // 2. Truncate to maximum 1000 characters
        if (cleaned.length() > 1000) {
            cleaned = cleaned.substring(0, 1000);
        }

        // 3. HTML / XSS escaping
        cleaned = HtmlUtils.htmlEscape(cleaned);

        // 4. Anti-spam: collapse excessive repeated characters (e.g. aaaaaaaaaaaa -> aaaaa)
        cleaned = SPAM_REPEATED_CHARS.matcher(cleaned).replaceAll("$1$1$1$1$1");

        // 5. Profanity masking
        for (String word : PROFANITY_WORDS) {
            String regex = "(?i)\\b" + Pattern.quote(word) + "\\b";
            String mask = "*".repeat(word.length());
            cleaned = cleaned.replaceAll(regex, mask);
        }

        return cleaned;
    }
}
