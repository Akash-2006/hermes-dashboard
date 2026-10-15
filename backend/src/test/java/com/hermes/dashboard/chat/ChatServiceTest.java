package com.hermes.dashboard.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private HermesAgentRunner agent;
    private ChatMessageRepository repository;
    private ChatService service;

    @BeforeEach
    void setUp() {
        agent = mock(HermesAgentRunner.class);
        repository = mock(ChatMessageRepository.class);
        when(repository.save(any(ChatMessage.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new ChatService(agent, repository);
    }

    @Test
    void sendsMessageAndStoresBothSides() {
        when(agent.ask("hi")).thenReturn("hello there");
        ChatMessage reply = service.sendUserMessage("hi");

        assertEquals("assistant", reply.getRole());
        assertEquals("hello there", reply.getContent());
        verify(repository).save(Mockito.argThat(m -> "user".equals(m.getRole()) && "hi".equals(m.getContent())));
        verify(repository).save(Mockito.argThat(m -> "assistant".equals(m.getRole())));
    }

    @Test
    void agentFailurePropagates() {
        when(agent.ask(any())).thenThrow(new IllegalStateException("hermes exited"));
        assertThrows(IllegalStateException.class, () -> service.sendUserMessage("hi"));
    }

    @Test
    void historyDelegatesToRepository() {
        when(repository.findTop100ByOrderByIdAsc())
                .thenReturn(List.of(new ChatMessage("user", "hi", null)));
        List<ChatMessage> history = service.history();
        assertEquals(1, history.size());
        assertEquals("hi", history.get(0).getContent());
    }
}
