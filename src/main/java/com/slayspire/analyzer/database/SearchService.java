package com.slayspire.analyzer.database;

import com.slayspire.analyzer.models.Card;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SearchService {
    private CardDAO cardDAO = new CardDAO();

    public List<Card> search(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return cardDAO.getAllCards();
        }
        return cardDAO.searchCards(query.trim());
    }

    public List<Card> searchWithNormalization(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String normalized = query.toLowerCase().replaceAll("[\\s\\-_]+", "");

        List<Card> allCards = cardDAO.getAllCards();
        return allCards.stream()
                .filter(c -> c.getNormalizedName().contains(normalized) ||
                             c.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}
