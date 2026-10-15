package com.hermes.dashboard.chat;

/**
 * Abstraction over the way the backend talks to the local Hermes agent.
 * Production runs the `hermes` CLI; tests inject a fake.
 */
public interface HermesAgentRunner {
    /** Sends one message to the agent and returns its reply. Throws when the agent fails. */
    String ask(String message);
}
