package com.github.rjhoppe.root_v2.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.jeremybrooks.knicker.AccountApi;
import net.jeremybrooks.knicker.Knicker.SourceDictionary;
import net.jeremybrooks.knicker.KnickerException;
import net.jeremybrooks.knicker.WordApi;
import net.jeremybrooks.knicker.WordsApi;
import net.jeremybrooks.knicker.dto.Definition;
import net.jeremybrooks.knicker.dto.TokenStatus;
import net.jeremybrooks.knicker.dto.Word;

/**
 * A client for interacting with the Wordnik API.
 * An API key is required for all operations.
 */
public class Wordnik {
    private final String apiKey;
    private final Set<String> commonWords;
    private final Cache<String, Boolean> validationCache;


    public Wordnik(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty.");
        }
        this.apiKey = apiKey;
        System.setProperty("WORDNIK_API_KEY", this.apiKey);

        this.commonWords = loadCommonWords();
        this.validationCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();
    }

    public void validate() throws Exception {
        System.out.println("Validating API key...");
        TokenStatus status = AccountApi.apiTokenStatus();
        if (status == null || !status.isValid()) {
            throw new IllegalStateException("API key is invalid.");
        }
        System.out.println("API key is valid.");
    }

    public String getWord() {
        Word wordResult;
        do {
            try {
                wordResult = WordsApi.randomWord();
            } catch (KnickerException e) {
                System.err.println("Error fetching random word: " + e.getMessage());
                return "";
            }
        } while (wordResult.getWord().length() < 7);

        return wordResult.getWord();
    }

    /**
      * Checks if a word is valid by looking for its definitions.
      * It first checks a local cache of common words, then a dynamic cache,
      * and finally falls back to the Wordnik API.
      * @param word The word to validate.
      * @return true if the word is considered valid, false otherwise.
      */
    public boolean validateWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        String normalizedWord = word.toLowerCase();

        // 1. Check the static set of common words
        if (commonWords.contains(normalizedWord)) {
            return true;
        }

        // 2. Check the dynamic cache
        try {
            return validationCache.get(normalizedWord, () -> {
                // 3. Fallback to the API if not in caches
                try {
                    LinkedHashSet<SourceDictionary> sources = new LinkedHashSet<>();
                    sources.add(SourceDictionary.ahd);
                    sources.add(SourceDictionary.wordnet);
                    sources.add(SourceDictionary.wiktionary);
                    List<Definition> definitions = WordApi.definitions(normalizedWord, sources);
                    return definitions != null && !definitions.isEmpty();
                } catch (Exception e) {
                    System.err.println("Submitted word '" + normalizedWord + "' is not valid: " + e.getMessage());
                    return false;
                }
            });
        } catch (ExecutionException e) {
            System.err.println("Error retrieving from cache for word '" + normalizedWord + "': " + e.getMessage());
            return false;
        }
    }

    private Set<String> loadCommonWords() {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("google-10000-english.txt")) {
            if (is == null) {
                System.err.println("Warning: Common words list not found.");
                return Collections.emptySet();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.toSet());
            }
        } catch (Exception e) {
            System.err.println("Error loading common words list: " + e.getMessage());
            return Collections.emptySet();
        }
    }
}
