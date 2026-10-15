package com.hermes.dashboard.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private AuthService authService;
    private AccountService accounts;
    private MockMvc mvc;

    private final Authentication hostAuth =
            new UsernamePasswordAuthenticationToken("akash", null, List.of());
    private final Authentication memberAuth =
            new UsernamePasswordAuthenticationToken("bob", null, List.of());

    private static org.springframework.test.web.servlet.request.RequestPostProcessor principal(String name) {
        return request -> {
            request.setUserPrincipal(new UsernamePasswordAuthenticationToken(name, null, List.of()));
            return request;
        };
    }

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        accounts = mock(AccountService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, accounts))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .build();
    }

    @Test
    void loginReturnsTokenAndRole() throws Exception {
        when(authService.login("akash", "pw")).thenReturn("tok123");
        when(accounts.isHost("akash")).thenReturn(true);
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"akash\",\"password\":\"pw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("tok123"))
                .andExpect(jsonPath("$.username").value("akash"))
                .andExpect(jsonPath("$.host").value(true));
    }

    @Test
    void badCredentialsReturn401() throws Exception {
        when(authService.login(any(), any())).thenReturn(null);
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bob\",\"password\":\"bad\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blankBodyReturns400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCreatesPendingAccount() throws Exception {
        when(accounts.register("newbie", "pw")).thenReturn(AccountService.RegisterResult.CREATED);
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newbie\",\"password\":\"pw\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void duplicateRegistrationReturns409() throws Exception {
        when(accounts.register("bob", "pw")).thenReturn(AccountService.RegisterResult.ALREADY_EXISTS);
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"bob\",\"password\":\"pw\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void pendingListIsHostOnly() throws Exception {
        when(authService.isHost(org.mockito.ArgumentMatchers.argThat(a -> a != null && "bob".equals(a.getName())))).thenReturn(false);
        when(authService.isHost(org.mockito.ArgumentMatchers.argThat(a -> a != null && "akash".equals(a.getName())))).thenReturn(true);
        when(accounts.pending()).thenReturn(List.of(
                new UserAccount("newbie", "h", UserAccount.Role.MEMBER, UserAccount.Status.PENDING)));

        mvc.perform(get("/api/auth/pending").with(principal("bob")))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/auth/pending").with(principal("akash")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("newbie"));
    }

    @Test
    void decideIsHostOnly() throws Exception {
        when(authService.isHost(org.mockito.ArgumentMatchers.argThat(a -> a != null && "bob".equals(a.getName())))).thenReturn(false);
        mvc.perform(post("/api/auth/decide").with(principal("bob"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newbie\",\"approve\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void hostCanApprove() throws Exception {
        when(authService.isHost(org.mockito.ArgumentMatchers.argThat(a -> a != null && "akash".equals(a.getName())))).thenReturn(true);
        when(accounts.decide("newbie", true)).thenReturn(true);
        mvc.perform(post("/api/auth/decide").with(principal("akash"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newbie\",\"approve\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("approved"));
    }
}
