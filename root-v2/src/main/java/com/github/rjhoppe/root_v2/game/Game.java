package com.github.rjhoppe.root_v2.game;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.rjhoppe.root_v2.dto.PlayerSubmission;
import com.github.rjhoppe.root_v2.utils.Wordnik;

import lombok.Getter;

@Getter
public class Game {
  private int score;
  private String playerName;
  private String gameStateMsg;
  private Map<Character, Integer> letterMap;
  private final String gameId;
  private final List<String> givenWords;
  private final List<String> submittedWords;
  private Instant timerStartTime;
  private Duration curTimerDuration;
  private boolean isTimerActive;
  private final Wordnik wordnikClient;

  public record GameOutput(int score, String playerName, String givenWords, String submittedWords) {}; 

  public Game(String playerName, String gameId, Wordnik wordnikClient) {
    if (wordnikClient == null) {
        throw new IllegalArgumentException("Wordnik client cannot be null.");
    }
    this.playerName = playerName;
    this.gameId = gameId;
    this.wordnikClient = wordnikClient;
    this.score = 0;
    this.gameStateMsg = "Let the game begin!";
    this.letterMap = new HashMap<>();
    this.givenWords = new ArrayList<>();
    this.submittedWords = new ArrayList<>();
    initializeGame();
  }

  public void startNewRoundTimer(Duration duration) {
    this.timerStartTime = Instant.now();
    this.curTimerDuration = duration;
    this.isTimerActive = true;

    this.gameStateMsg = "Timer started for " + duration.getSeconds() + " seconds";
    System.out.println("Game " + gameId + " - Timer started: " + timerStartTime);
  }

  public boolean isRoundTimerExpired() {
    if (!isTimerActive) {
      return false;
    }

    Instant now = Instant.now();
    Duration elapsed = Duration.between(timerStartTime, now);
    boolean expired = elapsed.compareTo(curTimerDuration) >= 0;

    if (expired) {
      this.isTimerActive = false;
      this.gameStateMsg = "Time's up!";
      System.out.println("Game " + gameId + " - Timer expired at: " + now);
    }

    return expired;
  }

  public Duration getRemainingTime() {
    if (!isTimerActive) {
      return Duration.ZERO;
    }

    Instant now = Instant.now();
    Duration elapsed = Duration.between(timerStartTime, now);
    Duration remaining = curTimerDuration.minus(elapsed);
    return remaining.isNegative() ? Duration.ZERO : remaining;
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

  public boolean submitWord(PlayerSubmission submission) {
    if (isRoundTimerExpired()) {
      this.gameStateMsg = "Timer has expired for gameId: " + this.gameId;
      System.out.println("Game " + gameId + " - Submission received after timer expired. Score: " + score);
      return false;
    }


    String word = submission.getWord();

    if (word.equals(this.submittedWords.get(this.submittedWords.size() - 1))) {
      this.gameStateMsg = "Submitted word cannot equal current game word";
      System.out.println(this.gameStateMsg);
      return false;
    }

    boolean isValid = this.wordnikClient.validateWord(word) && verifySubmittedLetters(word);
    if (isValid) {
      this.gameStateMsg = "Submitted word: " + word + " is valid. Awarding points...";
      System.out.println(this.gameStateMsg);
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

  private void initializeGame() {
    getNewWord();
    System.out.println("Game " + gameId + " initialized. First word: " + this.givenWords.get(this.givenWords.size() - 1));
    startNewRoundTimer(Duration.ofSeconds(60));
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
        this.gameStateMsg = "Submitted word: " + word + " is valid and the length of the current word. Awarding bonus points..";
        System.out.println(this.gameStateMsg);
        this.score += 500;
        if (!isRoundTimerExpired()) {
          getNewWord();
        }
    }
  }
}