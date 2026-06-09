package com.slayspire.analyzer.ui;

import com.slayspire.analyzer.database.*;
import com.slayspire.analyzer.models.Card;
import com.slayspire.analyzer.models.Relic;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StrategyPanel extends JPanel {
    private StrategyBuilder strategyBuilder;
    private CardDAO cardDAO;
    private RelicDAO relicDAO;
    private OddsCalculator oddsCalculator;

    private JComboBox<String> characterSelector;
    private DefaultListModel<String> cardListModel;
    private DefaultListModel<String> relicListModel;
    private JList<String> cardList;
    private JList<String> relicList;
    private JTextField searchCardField;
    private JTextField searchRelicField;
    private JList<String> searchResultsList;
    private JList<String> relicSearchResultsList;
    private DefaultListModel<String> searchResultsModel;
    private DefaultListModel<String> relicSearchResultsModel;
    private JTextArea oddsArea;
    private JLabel statusLabel;
    private JButton undoBtn;
    private JButton redoBtn;
    private JTextArea buildCardDetailArea;

    public StrategyPanel(CardDAO cardDAO, RelicDAO relicDAO, OddsCalculator oddsCalculator) {
        this.cardDAO = cardDAO;
        this.relicDAO = relicDAO;
        this.oddsCalculator = oddsCalculator;
        this.strategyBuilder = new StrategyBuilder();
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Class:"));
        characterSelector = new JComboBox<>();
        characterSelector.addItem("All");
        try {
            for (String color : cardDAO.getAllColors()) {
                characterSelector.addItem(capitalize(color));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        characterSelector.addActionListener(e -> updateOdds());
        topPanel.add(characterSelector);

        undoBtn = new JButton("Undo");
        undoBtn.addActionListener(e -> doUndo());
        topPanel.add(undoBtn);

        redoBtn = new JButton("Redo");
        redoBtn.addActionListener(e -> doRedo());
        topPanel.add(redoBtn);

        JButton clearBtn = new JButton("Clear Build");
        clearBtn.addActionListener(e -> clearBuild());
        topPanel.add(clearBtn);

        statusLabel = new JLabel(" ");
        topPanel.add(statusLabel);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        splitPane.setLeftComponent(createSearchPanel());
        splitPane.setRightComponent(createBuildPanel());

        add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Strategy Analysis"));
        oddsArea = new JTextArea(4, 50);
        oddsArea.setEditable(false);
        oddsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        bottomPanel.add(new JScrollPane(oddsArea), BorderLayout.CENTER);
        bottomPanel.setPreferredSize(new Dimension(0, 100));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Search Cards"));

        JPanel searchTop = new JPanel(new BorderLayout());
        searchCardField = new JTextField();
        searchCardField.addActionListener(e -> searchCards());
        searchTop.add(searchCardField, BorderLayout.CENTER);
        JButton searchBtn = new JButton("Search Cards");
        searchBtn.addActionListener(e -> searchCards());
        searchTop.add(searchBtn, BorderLayout.EAST);

        JButton addBtn = new JButton("Add to Build >>");
        addBtn.addActionListener(e -> addSelectedCard());
        searchTop.add(addBtn, BorderLayout.SOUTH);

        panel.add(searchTop, BorderLayout.NORTH);

        searchResultsModel = new DefaultListModel<>();
        searchResultsList = new JList<>(searchResultsModel);
        searchResultsList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        panel.add(new JScrollPane(searchResultsList), BorderLayout.CENTER);

        JPanel searchTopRelic = new JPanel(new BorderLayout());
        searchTopRelic.setBorder(BorderFactory.createTitledBorder("Search Relics"));
        searchRelicField = new JTextField();
        searchRelicField.addActionListener(e -> searchRelics());
        searchTopRelic.add(searchRelicField, BorderLayout.CENTER);
        JButton searchRelicBtn = new JButton("Search Relics");
        searchRelicBtn.addActionListener(e -> searchRelics());
        searchTopRelic.add(searchRelicBtn, BorderLayout.EAST);

        JButton addRelicBtn = new JButton("Add Relic to Build >>");
        addRelicBtn.addActionListener(e -> addSelectedRelic());
        searchTopRelic.add(addRelicBtn, BorderLayout.SOUTH);

        panel.add(searchTopRelic, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBuildPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Your Build"));

        JTabbedPane buildTabs = new JTabbedPane();

        cardListModel = new DefaultListModel<>();
        cardList = new JList<>(cardListModel);
        cardList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cardList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showBuildCardDetail();
        });
        JPanel cardPanel = new JPanel(new BorderLayout());
        JSplitPane cardSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        cardSplit.setResizeWeight(0.6);
        cardSplit.setTopComponent(new JScrollPane(cardList));
        buildCardDetailArea = new JTextArea(6, 30);
        buildCardDetailArea.setEditable(false);
        buildCardDetailArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        buildCardDetailArea.setLineWrap(true);
        buildCardDetailArea.setWrapStyleWord(true);
        cardSplit.setBottomComponent(new JScrollPane(buildCardDetailArea));
        cardPanel.add(cardSplit, BorderLayout.CENTER);
        JButton removeCardBtn = new JButton("Remove Selected Card");
        removeCardBtn.addActionListener(e -> removeSelectedCard());
        cardPanel.add(removeCardBtn, BorderLayout.SOUTH);
        buildTabs.addTab("Cards", cardPanel);

        relicListModel = new DefaultListModel<>();
        relicList = new JList<>(relicListModel);
        relicList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JPanel relicPanel = new JPanel(new BorderLayout());
        relicPanel.add(new JScrollPane(relicList), BorderLayout.CENTER);
        JButton removeRelicBtn = new JButton("Remove Selected Relic");
        removeRelicBtn.addActionListener(e -> removeSelectedRelic());
        relicPanel.add(removeRelicBtn, BorderLayout.SOUTH);
        buildTabs.addTab("Relics", relicPanel);

        panel.add(buildTabs, BorderLayout.CENTER);
        return panel;
    }

    private void searchCards() {
        String query = searchCardField.getText().trim();
        if (query.isEmpty()) return;
        try {
            SearchService ss = new SearchService();
            List<Card> results = ss.search(query);
            searchResultsModel.clear();
            if (results.isEmpty()) {
                String newQuery = JOptionPane.showInputDialog(this,
                        "No cards found for '" + query + "'. Enter a different name:",
                        "Not Found", JOptionPane.WARNING_MESSAGE);
                if (newQuery != null && !newQuery.trim().isEmpty()) {
                    searchCardField.setText(newQuery.trim());
                    searchCards();
                }
                return;
            }
            for (Card c : results) {
                searchResultsModel.addElement(c.getId() + "|" + c.getName() + " (" + c.getRarity() + ", " + capitalize(c.getColor()) + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchRelics() {
        String query = searchRelicField.getText().trim();
        if (query.isEmpty()) return;
        try {
            List<Relic> results = relicDAO.searchRelics(query);
            relicSearchResultsModel = new DefaultListModel<>();
            for (Relic r : results) {
                relicSearchResultsModel.addElement(r.getId() + "|" + r.getName() + " (" + r.getRarity() + ")");
            }
            if (results.isEmpty()) {
                relicSearchResultsModel.addElement("No relics found");
            }

            if (relicSearchResultsList == null) {
                relicSearchResultsList = new JList<>(relicSearchResultsModel);
                relicSearchResultsList.setFont(new Font("Monospaced", Font.PLAIN, 11));
                Component parent = searchRelicField.getParent().getParent();
                if (parent instanceof JPanel searchPanel) {
                    for (int i = 0; i < searchPanel.getComponentCount(); i++) {
                        Component c = searchPanel.getComponent(i);
                        if (c instanceof JScrollPane) {
                            ((JScrollPane) c).setViewportView(relicSearchResultsList);
                            break;
                        }
                    }
                }
            } else {
                relicSearchResultsList.setModel(relicSearchResultsModel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addSelectedCard() {
        String selected = searchResultsList.getSelectedValue();
        if (selected == null) return;
        try {
            String cardId = selected.split("\\|")[0];
            Card card = cardDAO.getCardById(cardId);
            if (card != null) {
                strategyBuilder.addCard(card);
                refreshBuildDisplay();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addSelectedRelic() {
        if (relicSearchResultsList == null) return;
        String selected = relicSearchResultsList.getSelectedValue();
        if (selected == null || selected.equals("No relics found")) return;
        try {
            String relicId = selected.split("\\|")[0];
            List<Relic> relics = relicDAO.searchRelics(relicId);
            if (!relics.isEmpty()) {
                strategyBuilder.addRelic(relics.get(0));
                refreshBuildDisplay();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showBuildCardDetail() {
        int idx = cardList.getSelectedIndex();
        if (idx >= 0) {
            List<Card> cards = strategyBuilder.getSelectedCards();
            if (idx < cards.size()) {
                Card c = cards.get(idx);
                StringBuilder d = new StringBuilder();
                d.append(c.getName()).append("  (").append(c.getCost()).append(" cost)\n");
                d.append(c.getType()).append("  |  ").append(c.getRarity()).append("\n");
                d.append("Class: ").append(capitalize(c.getColor())).append("\n\n");
                if (c.getDescription() != null) {
                    String desc = c.getDescription()
                            .replace("[gold]", "").replace("[/gold]", "")
                            .replace("[energy:1]", "[E]").replace("[energy:2]", "[EE]")
                            .replace("[energy:3]", "[EEE]");
                    d.append(desc).append("\n");
                }
                if (c.hasUpgrade() && c.getUpgradeDescription() != null) {
                    d.append("\nUpgrade: ").append(c.getUpgradeDescription()
                            .replace("[gold]", "").replace("[/gold]", "")
                            .replace("[energy:1]", "[E]").replace("[energy:2]", "[EE]")
                            .replace("[energy:3]", "[EEE]"));
                }
                buildCardDetailArea.setText(d.toString());
                buildCardDetailArea.setCaretPosition(0);
                return;
            }
        }
        buildCardDetailArea.setText("");
    }

    private void removeSelectedCard() {
        int idx = cardList.getSelectedIndex();
        if (idx >= 0) {
            strategyBuilder.removeCard(idx);
            refreshBuildDisplay();
        }
    }

    private void removeSelectedRelic() {
        int idx = relicList.getSelectedIndex();
        if (idx >= 0) {
            strategyBuilder.removeRelic(idx);
            refreshBuildDisplay();
        }
    }

    private void doUndo() {
        if (strategyBuilder.undo()) {
            refreshBuildDisplay();
        }
    }

    private void doRedo() {
        if (strategyBuilder.redo()) {
            refreshBuildDisplay();
        }
    }

    private void clearBuild() {
        strategyBuilder.clear();
        refreshBuildDisplay();
    }

    private void refreshBuildDisplay() {
        cardListModel.clear();
        buildCardDetailArea.setText("");
        for (Card c : strategyBuilder.getSelectedCards()) {
            cardListModel.addElement(c.getName() + " (" + c.getRarity() + ", " + c.getCost() + " cost)");
        }
        relicListModel.clear();
        for (Relic r : strategyBuilder.getSelectedRelics()) {
            relicListModel.addElement(r.getName() + " (" + r.getRarity() + ")");
        }
        updateOdds();
        undoBtn.setEnabled(strategyBuilder.canUndo());
        redoBtn.setEnabled(strategyBuilder.canRedo());
        statusLabel.setText("Build: " + cardListModel.size() + " cards, " + relicListModel.size() + " relics");
    }

    private void updateOdds() {
        StringBuilder sb = new StringBuilder();
        try {
            String selectedChar = characterSelector.getSelectedItem() == null ? "All" : characterSelector.getSelectedItem().toString();
            String color = selectedChar.equalsIgnoreCase("All") ? null : selectedChar.toLowerCase();

            List<String> raritiesInBuild = strategyBuilder.getSelectedCards().stream()
                    .map(Card::getRarity)
                    .distinct()
                    .collect(Collectors.toList());

            sb.append("Class: ").append(selectedChar).append("\n");

            if (color != null && !raritiesInBuild.isEmpty()) {
                double cumulative = oddsCalculator.getCumulativeOdds(color, raritiesInBuild);
                sb.append("Cumulative probability (finding at least one required card): ")
                        .append(String.format("%.1f%%", cumulative));

                if (cumulative < 50.0) {
                    sb.append(" \u26A0 WARNING: Below 50%! Consider adding substitute cards.");
                }
                sb.append("\n\n");

                for (String rarity : raritiesInBuild) {
                    double odds = oddsCalculator.getOdds(color, rarity);
                    int poolSize = oddsCalculator.getPoolSize(color, rarity);
                    int total = oddsCalculator.getTotalPoolSize(color);
                    sb.append(String.format("  %s: %.1f%% (%d/%d cards)", rarity, odds, poolSize, total)).append("\n");
                }
            } else if (color != null) {
                sb.append("Add cards to your build to see probability analysis.");
            } else {
                sb.append("Select a character to see probability analysis.");
            }

            oddsArea.setText(sb.toString());
            oddsArea.setCaretPosition(0);
        } catch (Exception e) {
            oddsArea.setText("Error calculating odds: " + e.getMessage());
        }
    }

    public StrategyBuilder getStrategyBuilder() { return strategyBuilder; }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
