package com.slayspire.analyzer.database;

import com.slayspire.analyzer.models.Card;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CardDAO {
    public List<Card> getAllCards() throws SQLException {
        List<Card> cards = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.ALL_CARDS)) {
            while (rs.next()) {
                cards.add(mapCard(rs));
            }
        }
        return cards;
    }

    public List<Card> searchCards(String query) throws SQLException {
        List<Card> cards = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlQueries.SEARCH_CARDS)) {
            stmt.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cards.add(mapCard(rs));
            }
        }
        return cards;
    }

    public List<Card> getFilteredCards(String color, String rarity, String type) throws SQLException {
        List<Card> cards = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SqlQueries.FILTER_CARDS_BASE);
        List<String> params = new ArrayList<>();

        if (color != null && !color.isEmpty() && !color.equals("All")) {
            sql.append(" AND color = ?");
            params.add(color);
        }
        if (rarity != null && !rarity.isEmpty() && !rarity.equals("All")) {
            sql.append(" AND rarity = ?");
            params.add(rarity);
        }
        if (type != null && !type.isEmpty() && !type.equals("All")) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        sql.append(" ORDER BY CAST(compendium_order AS INTEGER)");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cards.add(mapCard(rs));
            }
        }
        return cards;
    }

    public Card getCardById(String id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlQueries.CARD_BY_ID)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapCard(rs);
            }
        }
        return null;
    }

    public List<String> getAllColors() throws SQLException {
        List<String> colors = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.DISTINCT_CARD_COLORS)) {
            while (rs.next()) {
                colors.add(rs.getString("color"));
            }
        }
        return colors;
    }

    public List<String> getAllRarities() throws SQLException {
        List<String> rarities = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.DISTINCT_CARD_RARITIES)) {
            while (rs.next()) {
                rarities.add(rs.getString("rarity"));
            }
        }
        return rarities;
    }

    public List<String> getAllTypes() throws SQLException {
        List<String> types = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.DISTINCT_CARD_TYPES)) {
            while (rs.next()) {
                types.add(rs.getString("type"));
            }
        }
        return types;
    }

    public int getCardCountByColorAndRarity(String color, String rarity) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SqlQueries.CARD_COUNT_BY_COLOR_RARITY)) {
            stmt.setString(1, color);
            stmt.setString(2, rarity);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getTotalCardCount() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SqlQueries.TOTAL_CARD_COUNT)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    private Card mapCard(ResultSet rs) throws SQLException {
        Card card = new Card();
        card.setId(rs.getString("id"));
        card.setName(rs.getString("name"));
        card.setColor(rs.getString("color"));
        card.setRarity(rs.getString("rarity"));
        card.setType(rs.getString("type"));
        card.setTypeKey(rs.getString("type_key"));
        card.setCost(rs.getInt("cost"));
        card.setDescription(rs.getString("description"));
        card.setDescriptionRaw(rs.getString("description_raw"));
        card.setUpgrade(rs.getString("upgrade"));
        card.setUpgradeDescription(rs.getString("upgrade_description"));
        card.setKeywords(rs.getString("keywords"));
        card.setKeywordsKey(rs.getString("keywords_key"));
        card.setCompendiumOrder(rs.getString("compendium_order"));
        card.setXCost("1".equals(rs.getString("is_x_cost")));
        card.setXStarCost("1".equals(rs.getString("is_x_star_cost")));
        card.setDamage(rs.getString("damage"));
        card.setBlock(rs.getString("block"));
        card.setCardsDraw(rs.getString("cards_draw"));
        card.setEnergyGain(rs.getString("energy_gain"));
        return card;
    }
}
