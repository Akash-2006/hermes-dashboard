package com.hermes.dashboard.chat;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ChatService {

    private final HermesAgentRunner agent;
    private final ChatMessageRepository repository;

    public ChatService(HermesAgentRunner agent, ChatMessageRepository repository) {
        this.agent = agent;
        this.repository = repository;
    }

    public ChatMessage sendUserMessage(String message) {
        repository.save(new ChatMessage("user", message, Instant.now()));
        String reply = agent.ask(message);
        ChatMessage assistant = repository.save(new ChatMessage("assistant", reply, Instant.now()));
        return assistant;
    }

    public List<ChatMessage> history() {
        return repository.findTop100ByOrderByIdAsc();
    }
}
