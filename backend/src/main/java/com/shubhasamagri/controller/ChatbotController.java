package com.shubhasamagri.controller;

import com.shubhasamagri.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import java.util.Map;

/**
 * Proxy controller that forwards chat requests to the Python chatbot service.
 *
 * Architecture:
 *   React Frontend → Spring Boot /api/chatbot/* → Python FastAPI :8001/chat
 *
 * Benefits of proxying through Spring Boot:
 *   - Frontend only needs one base URL
 *   - Spring Security can authenticate before reaching chatbot
 *   - Centralized CORS handling
 *   - Can add rate limiting, logging here
 */
@RestController
@RequestMapping("/api/chatbot")
@Slf4j
@Tag(name = "Chatbot", description = "AI-powered pooja item recommendation chatbot")
public class ChatbotController {

    @Value("${chatbot.service.url:http://localhost:8001}")
    private String chatbotServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/chat")
    @Operation(
        summary = "Chat with Samagri Sakhi AI",
        description = "Send a message to the AI chatbot. It recommends pooja items based on occasion, region, and community."
    )
    public ResponseEntity<Map> chat(@RequestBody Map<String, Object> request) {
        try {
            String url = chatbotServiceUrl + "/chat";
            Map response = restTemplate.postForObject(url, request, Map.class);
            return ResponseEntity.ok(response);
        } catch (ResourceAccessException e) {
            log.warn("Chatbot service unavailable: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "response", "The AI assistant is currently starting up. Please try again in a moment. 🙏",
                "session_id", request.getOrDefault("session_id", ""),
                "user_context", Map.of()
            ));
        } catch (Exception e) {
            log.error("Chatbot proxy error: ", e);
            return ResponseEntity.ok(Map.of(
                "response", "Sorry, I'm having trouble responding right now. Please try again. 🙏",
                "session_id", request.getOrDefault("session_id", ""),
                "user_context", Map.of()
            ));
        }
    }

    @DeleteMapping("/chat/{sessionId}")
    @Operation(summary = "Clear a chat session")
    public ResponseEntity<Map> clearSession(@PathVariable String sessionId) {
        try {
            restTemplate.delete(chatbotServiceUrl + "/chat/" + sessionId);
            return ResponseEntity.ok(Map.of("message", "Session cleared"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "Session not found or already cleared"));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Check chatbot service health")
    public ResponseEntity<Map> health() {
        try {
            Map response = restTemplate.getForObject(chatbotServiceUrl + "/health", Map.class);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "unavailable",
                "message", "Chatbot service is not running. Start it with: cd chatbot && uvicorn main:app --port 8001"
            ));
        }
    }
}
