package com.slayspire.analyzer.ui;

import com.slayspire.analyzer.database.CardDAO;
import com.slayspire.analyzer.database.SearchService;
import com.slayspire.analyzer.models.Card;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CardCodexPanel extends JPanel {
    private CardDAO cardDAO;
    private SearchService searchService;
    private CardTableModel tableModel;
    private JTable cardTable;
    private JComboBox<String> colorFilter;
    private JComboBox<String> rarityFilter;
    private JComboBox<String> typeFilter;
    private JTextField searchField;
    private JLabel statusLabel;
    private List<Card> currentCards;
    private CardDetailPanel detailPanel;

    public CardCodexPanel(CardDAO cardDAO, SearchService searchService) {
        this.cardDAO = cardDAO;
        this.searchService = searchService;
        this.currentCards = new ArrayList<>();
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        loadFilters();
        refreshData();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.addActionListener(e -> refreshData());
        searchPanel.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> refreshData());
        searchPanel.add(searchBtn);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Character:"));
        colorFilter = new JComboBox<>();
        colorFilter.addActionListener(e -> refreshData());
        filterPanel.add(colorFilter);

        filterPanel.add(new JLabel("Rarity:"));
        rarityFilter = new JComboBox<>();
        rarityFilter.addActionListener(e -> refreshData());
        filterPanel.add(rarityFilter);

        filterPanel.add(new JLabel("Type:"));
        typeFilter = new JComboBox<>();
        typeFilter.addActionListener(e -> refreshData());
        filterPanel.add(typeFilter);

        JButton clearBtn = new JButton("Clear Filters");
        clearBtn.addActionListener(e -> clearFilters());
        filterPanel.add(clearBtn);

        JPanel topLeft = new JPanel(new BorderLayout());
        topLeft.add(searchPanel, BorderLayout.NORTH);
        topLeft.add(filterPanel, BorderLayout.SOUTH);
        topPanel.add(topLeft, BorderLayout.WEST);

        statusLabel = new JLabel(" ");
        topPanel.add(statusLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new CardTableModel();
        cardTable = new JTable(tableModel);
        cardTable.setAutoCreateRowSorter(true);
        cardTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showCardDetail();
            }
        });
        cardTable.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cardTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        cardTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        cardTable.getColumnModel().getColumn(2).setPreferredWidth(20);

        JScrollPane scrollPane = new JScrollPane(cardTable);
        add(scrollPane, BorderLayout.CENTER);

        detailPanel = new CardDetailPanel();
        add(detailPanel, BorderLayout.EAST);
    }

    private void loadFilters() {
        try {
            colorFilter.addItem("All");
            for (String color : cardDAO.getAllColors()) {
                colorFilter.addItem(capitalize(color));
            }
            rarityFilter.addItem("All");
            for (String r : cardDAO.getAllRarities()) {
                rarityFilter.addItem(r);
            }
            typeFilter.addItem("All");
            for (String t : cardDAO.getAllTypes()) {
                typeFilter.addItem(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshData() {
        try {
            String searchText = searchField.getText().trim();
            String color = getFilterValue(colorFilter);
            String rarity = getFilterValue(rarityFilter);
            String type = getFilterValue(typeFilter);

            List<Card> cards;
            boolean hasSearch = !searchText.isEmpty();
            boolean hasFilters = !color.equals("All") || !rarity.equals("All") || !type.equals("All");

            if (hasSearch) {
                cards = searchService.search(searchText);
                if (hasFilters) {
                    cards = cards.stream()
                            .filter(c -> color.equals("All") || c.getColor().equalsIgnoreCase(color))
                            .filter(c -> rarity.equals("All") || c.getRarity().equals(rarity))
                            .filter(c -> type.equals("All") || c.getType().equals(type))
                            .toList();
                }
            } else if (hasFilters) {
                cards = cardDAO.getFilteredCards(
                        color.equals("All") ? null : color.toLowerCase(),
                        rarity.equals("All") ? null : rarity,
                        type.equals("All") ? null : type);
            } else {
                cards = cardDAO.getAllCards();
            }

            currentCards = cards;
            tableModel.setCards(cards);
            statusLabel.setText(cards.size() + " cards found");
            detailPanel.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading cards");
        }
    }

    private void clearFilters() {
        colorFilter.setSelectedIndex(0);
        rarityFilter.setSelectedIndex(0);
        typeFilter.setSelectedIndex(0);
        searchField.setText("");
        refreshData();
    }

    private String getFilterValue(JComboBox<String> box) {
        Object selected = box.getSelectedItem();
        return selected == null ? "All" : selected.toString();
    }

    private void showCardDetail() {
        int row = cardTable.getSelectedRow();
        if (row >= 0 && row < currentCards.size()) {
            Card card = currentCards.get(row);
            detailPanel.showCard(card);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static class CardTableModel extends AbstractTableModel {
        private List<Card> cards = new ArrayList<>();
        private final String[] columns = {"#", "Name", "Cost", "Type", "Rarity", "Character"};

        void setCards(List<Card> cards) {
            this.cards = new ArrayList<>(cards);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return cards.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Card c = cards.get(row);
            return switch (col) {
                case 0 -> row + 1;
                case 1 -> c.getName();
                case 2 -> c.getFormattedCost();
                case 3 -> c.getType();
                case 4 -> c.getRarity();
                case 5 -> c.getColor();
                default -> "";
            };
        }
    }
}
