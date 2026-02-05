package com.github.rjhoppe.root_v2.controllers;

import com.github.rjhoppe.root_v2.game.GameManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerRateLimiterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameManager gameManager;

    // Note: The default rate limit is 5 requests per 10 seconds.
    private static final int RATE_LIMIT = 5;

    @Test
    void whenStartGameEndpointIsCalled_thenRateLimitingIsApplied() throws Exception {
        // First 5 requests should succeed
        for (int i = 0; i < RATE_LIMIT; i++) {
            mockMvc.perform(get("/api/game/start"))
                   .andExpect(status().isOk());
        }

        // 6th request should be blocked
        mockMvc.perform(get("/api/game/start"))
               .andExpect(status().isTooManyRequests());
    }

    @Test
    void whenSubmitEndpointIsCalled_thenRateLimitingIsApplied() throws Exception {
        // Create a game first to have a valid gameId
        String gameId = gameManager.createNewGame();

        // First 5 requests should succeed
        for (int i = 0; i < RATE_LIMIT; i++) {
            mockMvc.perform(post("/api/game/submit")
                       .param("gameId", gameId)
                       .param("word", "test_word_" + i))
                   .andExpect(status().isOk());
        }

        // 6th request should be blocked
        mockMvc.perform(post("/api/game/submit")
                   .param("gameId", gameId)
                   .param("word", "blocked_word"))
               .andExpect(status().isTooManyRequests());
    }
}
