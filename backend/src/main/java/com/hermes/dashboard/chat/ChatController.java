package com.hermes.dashboard.chat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    public record SendRequest(String message) {
        public boolean isBlank() {
            return message == null || message.isBlank();
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody SendRequest request) {
        if (request.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message is required"));
        }
        try {
            ChatMessage reply = chatService.sendUserMessage(request.message().trim());
            return ResponseEntity.ok(ChatMessageDto.from(reply));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(502).body(Map.of("error", "hermes agent unavailable: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public List<ChatMessageDto> history() {
        return chatService.history().stream().map(ChatMessageDto::from).toList();
    }
}
