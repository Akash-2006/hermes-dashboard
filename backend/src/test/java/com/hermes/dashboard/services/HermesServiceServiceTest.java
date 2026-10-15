package com.hermes.dashboard.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HermesServiceServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void listsInstalledSkills() throws IOException {
        Path skill = tempDir.resolve("skills").resolve("my-skill");
        Files.createDirectories(skill);
        Files.writeString(skill.resolve("SKILL.md"),
                "---\nname: my-skill\ndescription: \"Does a thing\"\n---\nBody");

        HermesServiceService service = new HermesServiceService(tempDir.toString());
        List<ServiceInfo> skills = service.listSkills();

        assertEquals(1, skills.size());
        assertEquals("my-skill", skills.get(0).name());
        assertEquals("Does a thing", skills.get(0).description());
    }

    @Test
    void missingHermesHomeReturnsEmptyList() {
        HermesServiceService service = new HermesServiceService(tempDir.resolve("nonexistent").toString());
        assertTrue(service.listSkills().isEmpty());
    }

    @Test
    void listsNestedCategorySkills() throws IOException {
        Path skill = tempDir.resolve("skills").resolve("media").resolve("youtube");
        Files.createDirectories(skill);
        Files.writeString(skill.resolve("SKILL.md"),
                "---\nname: youtube-content\ndescription: Transcripts\n---\nBody");

        HermesServiceService service = new HermesServiceService(tempDir.toString());
        List<ServiceInfo> skills = service.listSkills();
        assertEquals(1, skills.size());
        assertEquals("youtube-content", skills.get(0).name());
        assertEquals("youtube", skills.get(0).source());
    }

    @Test
    void directoriesWithoutSkillMdAreIgnored() throws IOException {
        Files.createDirectories(tempDir.resolve("skills").resolve("not-a-skill"));
        HermesServiceService service = new HermesServiceService(tempDir.toString());
        assertTrue(service.listSkills().isEmpty());
    }

    @Test
    void parsesQuotedFrontmatterValues() throws IOException {
        Path skill = tempDir.resolve("skills").resolve("quoted");
        Files.createDirectories(skill);
        Files.writeString(skill.resolve("SKILL.md"),
                "---\nname: 'quoted-name'\ndescription: \"A \\\"fancy\\\" desc\"\n---\nBody");

        HermesServiceService service = new HermesServiceService(tempDir.toString());
        assertEquals("quoted-name", service.listSkills().get(0).name());
    }

    @Test
    void statusReportsHomePresence() {
        HermesServiceService service = new HermesServiceService(tempDir.toString());
        assertTrue(service.hermesHomeExists());
    }
}
