package com.slayspire.analyzer.database;

import com.slayspire.analyzer.models.Relic;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RelicDAO {
    public List<Relic> getAllRelics() throws SQLException {
        List<Relic> relics = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.ALL_RELICS)) {
            while (rs.next()) {
                relics.add(mapRelic(rs));
            }
        }
        return relics;
    }

    public List<Relic> searchRelics(String query) throws SQLException {
        List<Relic> relics = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlQueries.SEARCH_RELICS)) {
            stmt.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                relics.add(mapRelic(rs));
            }
        }
        return relics;
    }

    public List<Relic> getFilteredRelics(String pool, String rarity) throws SQLException {
        List<Relic> relics = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SqlQueries.FILTER_RELICS_BASE);
        List<String> params = new ArrayList<>();

        if (pool != null && !pool.isEmpty() && !pool.equals("All")) {
            sql.append(" AND pool = ?");
            params.add(pool);
        }
        if (rarity != null && !rarity.isEmpty() && !rarity.equals("All")) {
            sql.append(" AND rarity = ?");
            params.add(rarity);
        }
        sql.append(" ORDER BY CAST(compendium_order AS INTEGER)");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                relics.add(mapRelic(rs));
            }
        }
        return relics;
    }

    public List<String> getAllPools() throws SQLException {
        List<String> pools = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.DISTINCT_RELIC_POOLS)) {
            while (rs.next()) {
                pools.add(rs.getString("pool"));
            }
        }
        return pools;
    }

    public List<String> getAllRelicRarities() throws SQLException {
        List<String> rarities = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.DISTINCT_RELIC_RARITIES)) {
            while (rs.next()) {
                rarities.add(rs.getString("rarity"));
            }
        }
        return rarities;
    }

    private Relic mapRelic(ResultSet rs) throws SQLException {
        Relic relic = new Relic();
        relic.setId(rs.getString("id"));
        relic.setName(rs.getString("name"));
        relic.setPool(rs.getString("pool"));
        relic.setRarity(rs.getString("rarity"));
        relic.setDescription(rs.getString("description"));
        relic.setDescriptionRaw(rs.getString("description_raw"));
        relic.setFlavor(rs.getString("flavor"));
        relic.setCompendiumOrder(rs.getString("compendium_order"));
        relic.setImageUrl(rs.getString("image_url"));
        relic.setMerchantPrice(rs.getString("merchant_price"));
        return relic;
    }
}
