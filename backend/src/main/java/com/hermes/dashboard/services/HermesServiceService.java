package com.hermes.dashboard.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Lists installed Hermes skills by reading SKILL.md frontmatter from
 * the skills directory of the Hermes home.
 */
@Service
public class HermesServiceService {

    private final Path hermesHome;

    public HermesServiceService(@Value("${hermes.dashboard.hermes-home}") String hermesHome) {
        this.hermesHome = Path.of(hermesHome);
    }

    public List<ServiceInfo> listSkills() {
        List<ServiceInfo> result = new ArrayList<>();
        Path skillsDir = hermesHome.resolve("skills");
        if (!Files.isDirectory(skillsDir)) {
            return result;
        }
        try (Stream<Path> categories = Files.list(skillsDir)) {
            categories.filter(Files::isDirectory).forEach(dir -> collectSkills(dir, result));
        } catch (IOException e) {
            return result;
        }
        return result;
    }

    /** Skills live at skills/<name>/SKILL.md or skills/<category>/<name>/SKILL.md. */
    private void collectSkills(Path dir, List<ServiceInfo> result) {
        Path skillMd = dir.resolve("SKILL.md");
        if (Files.isRegularFile(skillMd)) {
            Map<String, String> fm = parseFrontmatter(skillMd);
            result.add(new ServiceInfo(
                    fm.getOrDefault("name", dir.getFileName().toString()),
                    fm.getOrDefault("description", ""),
                    dir.getFileName().toString()));
            return;
        }
        try (Stream<Path> children = Files.list(dir)) {
            children.filter(Files::isDirectory).forEach(child -> collectSkills(child, result));
        } catch (IOException e) {
            // unreadable category dir: skip it
        }
    }

    public boolean hermesHomeExists() {
        return Files.isDirectory(hermesHome);
    }

    /** Minimal YAML frontmatter reader: '---', key: value lines, '---'. */
    static Map<String, String> parseFrontmatter(Path skillMd) {
        try {
            List<String> lines = Files.readAllLines(skillMd);
            Map<String, String> out = new java.util.HashMap<>();
            boolean inFrontmatter = false;
            boolean done = false;
            for (String line : lines) {
                String t = line.trim();
                if (!inFrontmatter && t.equals("---")) { inFrontmatter = true; continue; }
                if (inFrontmatter && t.equals("---")) { done = true; break; }
                if (inFrontmatter) {
                    int colon = t.indexOf(':');
                    if (colon > 0) {
                        String key = t.substring(0, colon).trim();
                        String value = t.substring(colon + 1).trim();
                        if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        } else if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
                            value = value.substring(1, value.length() - 1);
                        }
                        out.put(key, value);
                    }
                }
            }
            return done || inFrontmatter ? out : Map.of();
        } catch (IOException e) {
            return Map.of();
        }
    }
}
