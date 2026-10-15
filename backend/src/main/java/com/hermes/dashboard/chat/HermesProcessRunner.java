package com.hermes.dashboard.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Runs `hermes chat -q "<message>"` as a local subprocess.
 */
@Component
public class HermesProcessRunner implements HermesAgentRunner {

    private final String hermesCommand;
    private final long timeoutSeconds;

    public HermesProcessRunner(@Value("${hermes.dashboard.hermes-command}") String hermesCommand,
                               @Value("${hermes.dashboard.hermes-timeout-seconds:300}") long timeoutSeconds) {
        this.hermesCommand = hermesCommand;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public String ask(String message) {
        try {
            ProcessBuilder pb = new ProcessBuilder(hermesCommand, "chat", "-q", message);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output;
            try (var in = process.getInputStream()) {
                output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("hermes timed out after " + timeoutSeconds + "s");
            }
            int exit = process.exitValue();
            if (exit != 0) {
                throw new IllegalStateException("hermes exited with code " + exit + ": " + tail(output));
            }
            return stripBanner(output);
        } catch (IOException e) {
            throw new IllegalStateException("failed to run hermes: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for hermes", e);
        }
    }

    /**
     * `hermes chat -q` prints a startup banner plus a rich box around the reply:
     *   Query: ...\r\nInitializing agent...\r\n──\r\n\r\n╭─ ☤ Hermes ─...─╮\r\nREPLY\r\n╰─...─╯
     * Extracts just the reply. Falls back to the trimmed raw output.
     */
    static String stripBanner(String output) {
        String text = output.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = text.split("\n");
        int boxStart = -1;
        int boxEnd = -1;
        for (int i = lines.length - 1; i >= 0; i--) {
            String t = lines[i].trim();
            if (boxEnd == -1 && t.startsWith("╰")) boxEnd = i;
            else if (boxEnd != -1 && t.startsWith("╭")) { boxStart = i; break; }
        }
        if (boxStart != -1 && boxEnd != -1) {
            StringBuilder reply = new StringBuilder();
            for (int i = boxStart + 1; i < boxEnd; i++) {
                String line = lines[i];
                if (reply.length() > 0) reply.append('\n');
                reply.append(stripSideBorders(line));
            }
            String result = reply.toString().trim();
            if (!result.isEmpty()) return result;
        }
        return trimTrailingSummary(text).trim();
    }

    private static String stripSideBorders(String line) {
        String t = line.trim();
        if (t.startsWith("│")) t = t.substring(1);
        if (t.endsWith("│")) t = t.substring(0, t.length() - 1);
        return t.trim();
    }

    /** Falls back to stripping the banner and the one-shot summary when no box was printed. */
    private static String trimTrailingSummary(String text) {
        return text
                .replaceAll("(?m)^(Query:.*|Initializing agent\\.\\.\\..*)$", "")
                .replaceAll("(?m)^(__HERMES_CWD_.*__.*|Session:|Title:|Duration:|Messages:).*$", "")
                .trim();
    }

    private static String tail(String s) {
        if (s.length() <= 500) return s;
        return "..." + s.substring(s.length() - 500);
    }
}
