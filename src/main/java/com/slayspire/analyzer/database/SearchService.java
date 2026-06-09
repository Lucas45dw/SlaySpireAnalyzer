package com.slayspire.analyzer.database;

import com.slayspire.analyzer.models.Card;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SearchService {
    private CardDAO cardDAO = new CardDAO();
    private Card[] lastResults = new Card[0];

    public List<Card> search(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            List<Card> all = cardDAO.getAllCards();
            lastResults = all.toArray(new Card[0]);
            return all;
        }
        List<Card> results = cardDAO.searchCards(query.trim());
        lastResults = results.toArray(Card[]::new);
        return results;
    }

    public Card[] searchAsArray(String query) throws SQLException {
        search(query);
        return lastResults;
    }

    public List<Card> searchWithNormalization(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String normalized = query.toLowerCase().replaceAll("[\\s\\-_]+", "");

        List<Card> allCards = cardDAO.getAllCards();
        List<Card> results = allCards.stream()
                .filter(c -> c.getNormalizedName().contains(normalized) ||
                             c.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        lastResults = results.toArray(Card[]::new);
        return results;
    }
}
