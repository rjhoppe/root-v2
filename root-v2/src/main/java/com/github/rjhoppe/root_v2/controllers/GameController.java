package com.github.rjhoppe.root_v2.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.rjhoppe.root_v2.dto.PlayerSubmission;
import com.github.rjhoppe.root_v2.game.Game;
import com.github.rjhoppe.root_v2.game.GameManager;
import com.github.rjhoppe.root_v2.game.GameNotFoundException;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("api/game")
public class GameController {
  private final GameManager gameManager;

  @GetMapping("/start")
  public String startGame(Model model) {
    String gameId = gameManager.createNewGame();
    Game game = gameManager.getGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    
    model.addAttribute("game", game);
    model.addAttribute("remainingTimeSeconds", game.getRemainingTime().getSeconds());
    return "game-fragment :: gameContent"; 
  }
  
  
  @PostMapping("/submit")
  public String submitAction(@RequestParam String gameId, @ModelAttribute PlayerSubmission word, Model model) {
    try {
      Game updatedGame = gameManager.processPlayerSubmissions(gameId, word);
      model.addAttribute("game", updatedGame);
      model.addAttribute("remainingTimeSeconds", updatedGame.getRemainingTime().getSeconds());
      return "game-fragment :: gameContent";
    } catch (GameNotFoundException e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "error-fragment :: errorMessage";
    }
  }
}
