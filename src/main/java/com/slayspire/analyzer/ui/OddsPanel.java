package com.slayspire.analyzer.ui;

import com.slayspire.analyzer.database.CardDAO;
import com.slayspire.analyzer.database.OddsCalculator;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

public class OddsPanel extends JPanel {
    private OddsCalculator oddsCalculator;
    private CardDAO cardDAO;
    private JComboBox<String> characterSelector;
    private JTextArea oddsDisplay;
    private JLabel totalCardsLabel;

    public OddsPanel(CardDAO cardDAO, OddsCalculator oddsCalculator) {
        this.cardDAO = cardDAO;
        this.oddsCalculator = oddsCalculator;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        updateDisplay();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Character:"));
        characterSelector = new JComboBox<>();
        characterSelector.addItem("All");
        try {
            for (String color : cardDAO.getAllColors()) {
                characterSelector.addItem(capitalize(color));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        characterSelector.addActionListener(e -> updateDisplay());
        topPanel.add(characterSelector);

        totalCardsLabel = new JLabel(" ");
        topPanel.add(totalCardsLabel);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            try {
                oddsCalculator = new OddsCalculator();
                updateDisplay();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        topPanel.add(refreshBtn);

        add(topPanel, BorderLayout.NORTH);

        oddsDisplay = new JTextArea();
        oddsDisplay.setEditable(false);
        oddsDisplay.setFont(new Font("Monospaced", Font.PLAIN, 12));
        oddsDisplay.setLineWrap(true);
        oddsDisplay.setWrapStyleWord(true);
        add(new JScrollPane(oddsDisplay), BorderLayout.CENTER);
    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder();
        try {
            String selected = characterSelector.getSelectedItem() == null ? "All" : characterSelector.getSelectedItem().toString();
            String color = selected.equalsIgnoreCase("All") ? null : selected.toLowerCase();

            sb.append("Card Pool Analysis\n");
            sb.append("==================\n\n");

            if (color != null) {
                int total = oddsCalculator.getTotalPoolSize(color);
                sb.append("Character: ").append(selected).append("\n");
                sb.append("Total ").append(selected).append(" cards: ").append(total).append("\n\n");
                sb.append("Rarity Breakdown:\n");

                for (String rarity : cardDAO.getAllRarities()) {
                    int count = oddsCalculator.getPoolSize(color, rarity);
                    if (count > 0) {
                        double odds = oddsCalculator.getOdds(color, rarity);
                        sb.append(String.format("  %-15s %3d cards  (%5.1f%% chance)", rarity, count, odds)).append("\n");
                    }
                }

                sb.append("\nProbability of finding a specific card by rarity:\n");
                sb.append("  (Assuming uniform distribution within each rarity tier)\n\n");
                for (String rarity : cardDAO.getAllRarities()) {
                    int count = oddsCalculator.getPoolSize(color, rarity);
                    if (count > 0) {
                        double specificCardOdds = oddsCalculator.getOdds(color, rarity) / count;
                        sb.append(String.format("  %-15s 1/%d = %.2f%%", rarity, count, specificCardOdds)).append("\n");
                    }
                }
            } else {
                sb.append("Select a character above to see card pool odds.\n\n");
                sb.append("All Characters:\n");
                Map<String, Double> allOdds = oddsCalculator.getAllColorOdds();
                for (Map.Entry<String, Double> entry : allOdds.entrySet()) {
                    int total = oddsCalculator.getTotalPoolSize(entry.getKey());
                    sb.append(String.format("  %-15s %3d cards total", capitalize(entry.getKey()), total)).append("\n");
                }
            }

            int grandTotal = cardDAO.getTotalCardCount();
            totalCardsLabel.setText("Total cards in DB: " + grandTotal);

            oddsDisplay.setText(sb.toString());
            oddsDisplay.setCaretPosition(0);
        } catch (SQLException e) {
            oddsDisplay.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
