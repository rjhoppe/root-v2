package com.github.rjhoppe.root_v2.game;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.github.rjhoppe.root_v2.utils.Wordnik;

import lombok.Getter;

@Getter
public class Game {
  private int score;
  private String playerName;
  private Map<Character, Integer> letterMap;
  private final UUID gameId;
  private final List<String> givenWords;
  private final List<String> submittedWords;
  private final Wordnik wordnikClient;

  public record GameOutput(int score, String playerName, String givenWords, String submittedWords) {}; 

  public Game(String playerName, Wordnik wordnikClient) {
    if (wordnikClient == null) {
        throw new IllegalArgumentException("Wordnik client cannot be null.");
    }
    this.playerName = playerName;
    this.gameId = UUID.randomUUID();
    this.wordnikClient = wordnikClient;
    this.score = 0;
    this.letterMap = new HashMap<>();
    this.givenWords = new ArrayList<>();
    this.submittedWords = new ArrayList<>();
  }

  public void getNewWord() {
    String gameWord = this.wordnikClient.getWord();
    if (gameWord.isEmpty()) {
      System.err.println("Error initializing game");
      return;
    }
    this.letterMap = createLetterMap(gameWord);
    this.givenWords.add(gameWord);
  }

  public boolean submitWord(String word) {
    boolean isValid = this.wordnikClient.validateWord(word) && verifySubmittedLetters(word);
    if (isValid) {
      this.submittedWords.add(word);
      this.score += word.length() * 50;
      applyBonusPoints(word);
    }
    return isValid;
  }

  public GameOutput endGame() {
    String combGivenWords = this.givenWords.toString();
    String combSubmittedWords = this.submittedWords.toString();

    return new GameOutput(
      this.score, 
      this.playerName, 
      combGivenWords, 
      combSubmittedWords
      );
  }

  private Map<Character, Integer> createLetterMap(String word) {
    Map<Character, Integer> letterMap = new HashMap<>();
    for (char letter: word.toCharArray()) {
      letterMap.put(letter, letterMap.getOrDefault(letter, 0) + 1);
    }
    return letterMap;
  }

  private boolean verifySubmittedLetters(String word) {
    Map<Character, Integer> subLetterMap = createLetterMap(word);

    for (Map.Entry<Character, Integer> entry: subLetterMap.entrySet()) {
      char letter = entry.getKey();
      int maxCount = entry.getValue();
      int availableCount = this.letterMap.getOrDefault(letter, 0);

      if (maxCount > availableCount) {
        return false;
      }

      subLetterMap.put(letter, maxCount - 1);
    }

    this.letterMap = subLetterMap;
    return true;
  }

  private void applyBonusPoints(String word) {
    if (word.length() == this.givenWords.get(0).length()) {
        this.score += 500;
        getNewWord();
    }
  }
}