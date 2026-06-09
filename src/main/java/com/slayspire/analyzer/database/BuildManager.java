package com.slayspire.analyzer.database;

import com.slayspire.analyzer.models.Card;
import com.slayspire.analyzer.models.Relic;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BuildManager {
    private static final String SAVES_DIR = "builds";

    public BuildManager() {
        try {
            Files.createDirectories(Paths.get(SAVES_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveBuild(String name, List<Card> cards, List<Relic> relics) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Build name cannot be empty");
        }
        if (cards.isEmpty() && relics.isEmpty()) {
            throw new IllegalStateException("Cannot save an empty build");
        }

        String filename = SAVES_DIR + "/" + sanitizeFilename(name) + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("# Slay the Spire 2 Build: " + name);
            writer.println("# Saved: " + new Date());
            writer.println("# Cards:");
            writer.println("CARDS:" + cards.size());
            for (Card card : cards) {
                writer.println(card.getId() + "|" + card.getName() + "|" + card.getColor() + "|" + card.getRarity());
            }
            writer.println("# Relics:");
            writer.println("RELICS:" + relics.size());
            for (Relic relic : relics) {
                writer.println(relic.getId() + "|" + relic.getName() + "|" + relic.getPool() + "|" + relic.getRarity());
            }
        }
    }

    public List<String> listSavedBuilds() {
        List<String> builds = new ArrayList<>();
        File dir = new File(SAVES_DIR);
        if (dir.exists()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File f : files) {
                    builds.add(f.getName().replace(".txt", ""));
                }
            }
        }
        builds.sort(String.CASE_INSENSITIVE_ORDER);
        return builds;
    }

    public boolean buildExists(String name) {
        String filename = SAVES_DIR + "/" + sanitizeFilename(name) + ".txt";
        return new File(filename).exists();
    }

    public String readRawFile(String name) throws IOException {
        String filename = SAVES_DIR + "/" + sanitizeFilename(name) + ".txt";
        return Files.readString(Paths.get(filename));
    }

    public BuildData loadBuild(String name) throws IOException {
        String filename = SAVES_DIR + "/" + sanitizeFilename(name) + ".txt";
        List<Card> cards = new ArrayList<>();
        List<Relic> relics = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean readingCards = false;
            boolean readingRelics = false;
            int cardCount = 0;
            int relicCount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                if (line.startsWith("CARDS:")) {
                    cardCount = Integer.parseInt(line.substring(6));
                    readingCards = true;
                    readingRelics = false;
                    continue;
                }
                if (line.startsWith("RELICS:")) {
                    relicCount = Integer.parseInt(line.substring(7));
                    readingRelics = true;
                    readingCards = false;
                    continue;
                }
                if (readingCards && cards.size() < cardCount) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2) {
                        Card card = new Card(parts[0], parts[1],
                                parts.length > 2 ? parts[2] : "",
                                parts.length > 3 ? parts[3] : "", "", 0, "", "", "");
                        cards.add(card);
                    }
                }
                if (readingRelics && relics.size() < relicCount) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 2) {
                        Relic relic = new Relic(parts[0], parts[1],
                                parts.length > 2 ? parts[2] : "",
                                parts.length > 3 ? parts[3] : "", "");
                        relics.add(relic);
                    }
                }
            }
        }
        return new BuildData(name, cards, relics);
    }

    public void deleteBuild(String name) {
        String filename = SAVES_DIR + "/" + sanitizeFilename(name) + ".txt";
        new File(filename).delete();
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    public static class BuildData {
        public final String name;
        public final List<Card> cards;
        public final List<Relic> relics;

        BuildData(String name, List<Card> cards, List<Relic> relics) {
            this.name = name;
            this.cards = cards;
            this.relics = relics;
        }
    }
}
