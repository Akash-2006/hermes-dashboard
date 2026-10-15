package com.hermes.dashboard.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HermesProcessRunnerTest {

    @Test
    void extractsReplyFromRichBox() {
        String output = "Query: hi\r\nInitializing agent...\r\n────\r\n\r\n"
                + "╭─ ☤ Hermes ─────────────╮\r\nHERMES-OK\r\n╰────────────────────────╯";
        assertEquals("HERMES-OK", HermesProcessRunner.stripBanner(output));
    }

    @Test
    void extractsMultiLineReplyAndStripsSideBorders() {
        String output = "banner\n╭─ Hermes ─╮\n│ line one │\n│ line two │\n╰──────────╯";
        assertEquals("line one\nline two", HermesProcessRunner.stripBanner(output));
    }

    @Test
    void fallsBackWhenNoBoxPresent() {
        String output = "Query: hi\nInitializing agent...\njust the answer";
        assertEquals("just the answer", HermesProcessRunner.stripBanner(output));
    }

    @Test
    void dropsOneShotSummaryLinesWhenNoBox() {
        String output = "answer text\nSession:        20261015_060406_d0039d\n"
                + "Title:          Something\nDuration:       22s\nMessages:       2 (1 user, 0 tool calls)\n"
                + "__HERMES_CWD_4760b4f52ef8__/home/userland__HERMES_CWD_4760b4f52ef8__";
        assertEquals("answer text", HermesProcessRunner.stripBanner(output));
    }
}
