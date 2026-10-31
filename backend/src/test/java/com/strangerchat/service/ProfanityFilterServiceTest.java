package com.strangerchat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfanityFilterServiceTest {

    private ProfanityFilterService filterService;

    @BeforeEach
    void setUp() {
        filterService = new ProfanityFilterService();
    }

    @Test
    void testXssEscaping() {
        String input = "<script>alert('pwned')</script>";
        String cleaned = filterService.sanitizeAndFilter(input);
        assertFalse(cleaned.contains("<script>"));
        assertTrue(cleaned.contains("&lt;script&gt;"));
    }

    @Test
    void testProfanityMasking() {
        String input = "Hello you cunt have a nice day";
        String cleaned = filterService.sanitizeAndFilter(input);
        assertFalse(cleaned.contains("cunt"));
        assertTrue(cleaned.contains("****"));
    }

    @Test
    void testSpamCharacterCollapse() {
        String input = "hellooooooooooooooooooooooooo world";
        String cleaned = filterService.sanitizeAndFilter(input);
        assertTrue(cleaned.contains("hellooooo world"));
        assertFalse(cleaned.contains("hellooooooooooooooooooooooooo"));
    }

    @Test
    void testLengthTruncation() {
        String input = "a".repeat(1200);
        String cleaned = filterService.sanitizeAndFilter(input);
        assertTrue(cleaned.length() <= 1000);
    }
}
