package com.slayspire.analyzer.database;

import java.sql.*;
import java.util.*;

public class OddsCalculator {
    private CardDAO cardDAO = new CardDAO();
    private Map<String, Integer> totalCountCache;
    private Map<String, Map<String, Integer>> rarityCountCache;

    public OddsCalculator() throws SQLException {
        buildCache();
    }

    private void buildCache() throws SQLException {
        totalCountCache = new HashMap<>();
        rarityCountCache = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.CARD_POOL_GROUP)) {
            while (rs.next()) {
                String color = rs.getString("color");
                String rarity = rs.getString("rarity");
                int count = rs.getInt("cnt");

                totalCountCache.merge(color, count, Integer::sum);
                rarityCountCache.computeIfAbsent(color, k -> new HashMap<>())
                        .put(rarity, count);
            }
        }
    }

    public double getOdds(String color, String rarity) {
        if (!rarityCountCache.containsKey(color)) return 0.0;
        Map<String, Integer> rarities = rarityCountCache.get(color);
        int total = totalCountCache.getOrDefault(color, 0);
        int count = rarities.getOrDefault(rarity, 0);
        if (total == 0) return 0.0;
        return (double) count / total * 100;
    }

    public int getPoolSize(String color, String rarity) {
        if (!rarityCountCache.containsKey(color)) return 0;
        return rarityCountCache.get(color).getOrDefault(rarity, 0);
    }

    public int getTotalPoolSize(String color) {
        return totalCountCache.getOrDefault(color, 0);
    }

    public double getCumulativeOdds(String color, List<String> rarities) {
        if (rarities.isEmpty()) return 0.0;
        double probNone = 1.0;
        for (String rarity : rarities) {
            double p = getOdds(color, rarity) / 100.0;
            probNone *= (1.0 - p);
        }
        return (1.0 - probNone) * 100;
    }

    public Map<String, Double> getAllColorOdds() throws SQLException {
        Map<String, Double> odds = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.DISTINCT_COLORS)) {
            while (rs.next()) {
                String color = rs.getString("color");
                double totalOdds = 0;
                for (String rarity : rarityCountCache.getOrDefault(color, Map.of()).keySet()) {
                    totalOdds += getOdds(color, rarity);
                }
                odds.put(color, totalOdds);
            }
        }
        return odds;
    }
}
