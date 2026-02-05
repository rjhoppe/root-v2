package com.github.rjhoppe.root_v2.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;

import net.jeremybrooks.knicker.WordApi;
import net.jeremybrooks.knicker.dto.Definition;

public class WordnikTest {

    private Wordnik wordnik;
    private static final String DUMMY_API_KEY = "dummy_api_key";

    @BeforeEach
    void setUp() {
        // Initialize Wordnik with a dummy API key
        wordnik = new Wordnik(DUMMY_API_KEY);
    }

    @Test
    void testValidateWord_commonWord() {
        // "the" is in the google-10000-english.txt list
        assertTrue(wordnik.validateWord("the"), "Common word 'the' should be valid.");
        assertTrue(wordnik.validateWord("The"), "Common word 'The' (capitalized) should be valid.");
    }

    @Test
    void testValidateWord_apiCallAndCache() {
        try (MockedStatic<WordApi> mockedWordApi = Mockito.mockStatic(WordApi.class)) {
            // Mock API to return definitions for "asdfghjkl"
            mockedWordApi.when(() -> WordApi.definitions(
                    eq("asdfghjkl"), any(LinkedHashSet.class)))
                    .thenReturn(Arrays.asList(new Definition()));

            // First call: should hit API
            assertTrue(wordnik.validateWord("asdfghjkl"), "Word 'asdfghjkl' should be valid via API.");

            // Verify API was called once for "asdfghjkl"
            mockedWordApi.verify(() -> WordApi.definitions(eq("asdfghjkl"), any(LinkedHashSet.class)), times(1));

            // Second call: should be cached, no API call
            assertTrue(wordnik.validateWord("asdfghjkl"), "Word 'asdfghjkl' should be valid via cache.");

            // Verify API was still called only once
            mockedWordApi.verify(() -> WordApi.definitions(eq("asdfghjkl"), any(LinkedHashSet.class)), times(1));

            // Test another word "foobar" that is not valid
            mockedWordApi.when(() -> WordApi.definitions(
                    eq("foobar"), any(LinkedHashSet.class)))
                    .thenReturn(Collections.emptyList());

            assertFalse(wordnik.validateWord("foobar"), "Word 'foobar' should be invalid via API.");
            mockedWordApi.verify(() -> WordApi.definitions(eq("foobar"), any(LinkedHashSet.class)), times(1));

            // Second call for "foobar" should be cached
            assertFalse(wordnik.validateWord("foobar"), "Word 'foobar' should be invalid via cache.");
            mockedWordApi.verify(() -> WordApi.definitions(eq("foobar"), any(LinkedHashSet.class)), times(1));
        }
    }

    @Test
    void testValidateWord_apiCallInvalid() {
        try (MockedStatic<WordApi> mockedWordApi = Mockito.mockStatic(WordApi.class)) {
            // Mock API to return no definitions for an unknown word
            mockedWordApi.when(() -> WordApi.definitions(
                    eq("unknownword"), any(LinkedHashSet.class)))
                    .thenReturn(Collections.emptyList());

            assertFalse(wordnik.validateWord("unknownword"), "Unknown word should be invalid.");
            mockedWordApi.verify(() -> WordApi.definitions(eq("unknownword"), any(LinkedHashSet.class)), times(1));
        }
    }

    @Test
    void testValidateWord_nullOrEmpty() {
        assertFalse(wordnik.validateWord(null), "Null word should be invalid.");
        assertFalse(wordnik.validateWord(""), "Empty word should be invalid.");
        assertFalse(wordnik.validateWord("   "), "Whitespace word should be invalid.");
    }

    @Test
    void testValidateWord_apiException() {
        try (MockedStatic<WordApi> mockedWordApi = Mockito.mockStatic(WordApi.class)) {
            // Mock API to throw an exception
            mockedWordApi.when(() -> WordApi.definitions(
                    eq("errorword"), any(LinkedHashSet.class)))
                    .thenThrow(new RuntimeException("API error"));

            assertFalse(wordnik.validateWord("errorword"), "Word causing API exception should be invalid.");
            mockedWordApi.verify(() -> WordApi.definitions(eq("errorword"), any(LinkedHashSet.class)), times(1));

            // Subsequent call should also return false due to caching of the exception result (false)
            assertFalse(wordnik.validateWord("errorword"), "Word causing API exception should still be invalid from cache.");
            mockedWordApi.verify(() -> WordApi.definitions(eq("errorword"), any(LinkedHashSet.class)), times(1));
        }
    }
}
