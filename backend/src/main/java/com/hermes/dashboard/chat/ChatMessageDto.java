package com.hermes.dashboard.chat;

public record ChatMessageDto(String role, String content, String createdAt) {

    public static ChatMessageDto from(ChatMessage m) {
        return new ChatMessageDto(m.getRole(), m.getContent(), m.getCreatedAt() == null ? null : m.getCreatedAt().toString());
    }
}
