package com.rks.collections.controller;

import com.rks.collections.dto.AuthDto;
import com.rks.collections.model.ChatMessage;
import com.rks.collections.security.JwtUtil;
import com.rks.collections.service.AiAssistantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired private AiAssistantService aiService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody AuthDto.ChatRequest req,
                                   @RequestHeader("Authorization") String authHeader) {
        String username = jwtUtil.extractUsername(authHeader.substring(7));
        String response = aiService.chat(username, req.getMessage(), req.getProductContext());
        return ResponseEntity.ok(Map.of(
            "response", response,
            "role", "assistant"
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> history(
            @RequestHeader("Authorization") String authHeader) {
        String username = jwtUtil.extractUsername(authHeader.substring(7));
        return ResponseEntity.ok(aiService.getChatHistory(username));
    }

    @DeleteMapping("/history")
    public ResponseEntity<?> clearHistory(@RequestHeader("Authorization") String authHeader) {
        String username = jwtUtil.extractUsername(authHeader.substring(7));
        aiService.clearHistory(username);
        return ResponseEntity.ok(Map.of("status", "cleared"));
    }
}
