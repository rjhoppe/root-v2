package com.github.rjhoppe.root_v2.game;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GameNotFoundException extends RuntimeException {

  public GameNotFoundException(String gameId) {
    super("Game with ID '" + gameId + "' not found.");
  }
}
