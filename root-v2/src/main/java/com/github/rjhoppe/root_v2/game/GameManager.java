package com.github.rjhoppe.root_v2.game;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.github.rjhoppe.root_v2.dto.PlayerSubmission;
import com.github.rjhoppe.root_v2.utils.Wordnik;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameManager {
  private final Map<String, Game> activeGames = new ConcurrentHashMap<>();
  private final Wordnik wordnikClient;

  public String createNewGame() {
    String gameId = UUID.randomUUID().toString();
    Game newGame = new Game("Player 1", gameId, wordnikClient);

    activeGames.put(gameId, newGame);
    return gameId;
  }

  public Optional<Game> getGame(String gameId) {
    // Do we need to account for what if the game is not in activeGames or has already been removed?
    return Optional.ofNullable(activeGames.get(gameId));
  }

  public void removeGame(String gameId) {
    Game game = activeGames.remove(gameId);
    if (game != null) {
      // change this to a more robust logging solution -> log.info
      System.out.println("Removed game instance: " + gameId);
    }
  }

  public Game processPlayerSubmissions(String gameId, PlayerSubmission submission) {
    Game game = getGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    if (game == null) {
      System.err.println("Attempted submission for non-existent game: " + gameId);
      // Do we want to be returning null here?
      return null;
    }

    boolean isSubmitted = game.submitWord(submission);
    if (isSubmitted) {
      game.getNewWord();
    }

    return game;
  }
}
