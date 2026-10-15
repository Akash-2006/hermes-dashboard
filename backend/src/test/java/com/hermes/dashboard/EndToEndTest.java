package com.hermes.dashboard;

import com.hermes.dashboard.chat.ChatMessageRepository;
import com.hermes.dashboard.chat.HermesAgentRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end: real HTTP server, real security chain, fake agent behind the seam.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "hermes.dashboard.host-username=admin",
                "hermes.dashboard.host-password=secret",
                "hermes.dashboard.jwt-secret=e2e-test-secret-that-is-long-enough-for-hs256!!"
        })
class EndToEndTest {

    @TestConfiguration
    static class FakeAgentConfig {
        @Bean
        @Primary
        HermesAgentRunner hermesAgentRunner() {
            return message -> "FAKE-REPLY:" + message;
        }
    }

    @org.springframework.boot.test.web.server.LocalServerPort
    int port;

    private final RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpEntity<Map<String, Object>> json(Map<String, Object> body) {
        return json(body, null);
    }

    private HttpEntity<Map<String, Object>> json(Map<String, Object> body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.setBearerAuth(token);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void registerThenPendingLoginBlockedThenHostApprovesThenLoginWorks() {
        // register a new member
        ResponseEntity<String> register = rest.postForEntity(baseUrl("/api/auth/register"),
                json(Map.of("username", "e2e-member", "password", "pw")), String.class);
        assertEquals(200, register.getStatusCode().value());

        // pending account cannot log in
        try {
            rest.postForEntity(baseUrl("/api/auth/login"),
                    json(Map.of("username", "e2e-member", "password", "pw")), String.class);
            throw new AssertionError("pending login should fail");
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            assertEquals(401, e.getStatusCode().value());
        }

        // host logs in and approves
        ResponseEntity<Map> hostLogin = rest.postForEntity(baseUrl("/api/auth/login"),
                json(Map.of("username", "admin", "password", "secret")), Map.class);
        assertEquals(200, hostLogin.getStatusCode().value());
        assertTrue((Boolean) hostLogin.getBody().get("host"), "seeded account must be host");
        String hostToken = (String) hostLogin.getBody().get("token");

        ResponseEntity<Map[]> pending = rest.exchange(baseUrl("/api/auth/pending"),
                HttpMethod.GET, new HttpEntity<>(jsonHeaders(hostToken)), Map[].class);
        assertEquals(200, pending.getStatusCode().value());
        assertTrue(java.util.Arrays.stream(pending.getBody())
                .anyMatch(m -> "e2e-member".equals(m.get("username"))));

        ResponseEntity<Map> decide = rest.exchange(baseUrl("/api/auth/decide"), HttpMethod.POST,
                json(Map.of("username", "e2e-member", "approve", true), hostToken), Map.class);
        assertEquals(200, decide.getStatusCode().value());

        // now the member can log in, and is not a host
        ResponseEntity<Map> memberLogin = rest.postForEntity(baseUrl("/api/auth/login"),
                json(Map.of("username", "e2e-member", "password", "pw")), Map.class);
        assertEquals(200, memberLogin.getStatusCode().value());
        assertFalse((Boolean) memberLogin.getBody().get("host"));

        // non-host cannot list pending accounts
        String memberToken = (String) memberLogin.getBody().get("token");
        try {
            rest.exchange(baseUrl("/api/auth/pending"), HttpMethod.GET,
                    new HttpEntity<>(jsonHeaders(memberToken)), Map[].class);
            throw new AssertionError("member should not list pending accounts");
        } catch (org.springframework.web.client.HttpClientErrorException.Forbidden e) {
            assertEquals(403, e.getStatusCode().value());
        }
    }

    private HttpHeaders jsonHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void protectedEndpointRejectedWithoutToken() {
        RestTemplate noRedirect = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        try {
            noRedirect.getForEntity(baseUrl("/api/chat/history"), String.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(401, e.getStatusCode().value());
            return;
        }
        throw new AssertionError("expected 401");
    }

    @Test
    void loginThenChatThenHistory() {
        ResponseEntity<Map> login = rest.postForEntity(baseUrl("/api/auth/login"),
                json(Map.of("username", "admin", "password", "secret")), Map.class);
        assertEquals(200, login.getStatusCode().value());
        String token = (String) login.getBody().get("token");
        assertNotNull(token);

        ResponseEntity<Map> send = rest.exchange(baseUrl("/api/chat/send"), HttpMethod.POST,
                json(Map.of("message", "ping"), token), Map.class);
        assertEquals(200, send.getStatusCode().value());
        assertEquals("FAKE-REPLY:ping", send.getBody().get("content"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map[]> history = rest.exchange(baseUrl("/api/chat/history"),
                HttpMethod.GET, new HttpEntity<>(headers), Map[].class);
        assertEquals(200, history.getStatusCode().value());
        assertTrue(history.getBody().length >= 2);
    }

    @Test
    void wrongPasswordRejected() {
        try {
            rest.postForEntity(baseUrl("/api/auth/login"),
                    json(Map.of("username", "admin", "password", "nope")), String.class);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(401, e.getStatusCode().value());
            return;
        }
        throw new AssertionError("expected 401");
    }
}
