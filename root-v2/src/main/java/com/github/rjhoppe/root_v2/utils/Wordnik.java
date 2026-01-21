package com.github.rjhoppe.root_v2.utils;
import java.util.LinkedHashSet;
import java.util.List;

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

    public Wordnik(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty.");
        }
        this.apiKey = apiKey;
        System.setProperty("WORDNIK_API_KEY", this.apiKey);
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
      * @param word The word to validate.
      * @return true if the word has definitions, false otherwise.
      */
    public boolean validateWord(String word) {
        try {
            LinkedHashSet<SourceDictionary> sources = new LinkedHashSet<>();
            sources.add(SourceDictionary.ahd);
            sources.add(SourceDictionary.wordnet);
            sources.add(SourceDictionary.wiktionary);
            List<Definition> definitions = WordApi.definitions(word, sources);
            return definitions != null && !definitions.isEmpty();
        } catch (Exception e) {
            System.err.println("Submitted word is not valid: " + e.getMessage());
            return false;
        }
    }
}