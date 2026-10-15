package com.hermes.dashboard.chat;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    @Test
    void sendReturnsReply() throws Exception {
        ChatService chatService = mock(ChatService.class);
        when(chatService.sendUserMessage("hi"))
                .thenReturn(new ChatMessage("assistant", "hello!", null));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ChatController(chatService)).build();

        mvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("assistant"))
                .andExpect(jsonPath("$.content").value("hello!"));
    }

    @Test
    void blankMessageRejected() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ChatController(mock(ChatService.class))).build();
        mvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agentFailureReturns502() throws Exception {
        ChatService chatService = mock(ChatService.class);
        when(chatService.sendUserMessage(any())).thenThrow(new IllegalStateException("hermes exited with code 1"));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ChatController(chatService)).build();

        mvc.perform(post("/api/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"hi\"}"))
                .andExpect(status().is(502));
    }

    @Test
    void historyReturnsList() throws Exception {
        ChatService chatService = mock(ChatService.class);
        when(chatService.history()).thenReturn(java.util.List.of(new ChatMessage("user", "hi", null)));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ChatController(chatService)).build();

        mvc.perform(get("/api/chat/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("hi"));
    }
}
