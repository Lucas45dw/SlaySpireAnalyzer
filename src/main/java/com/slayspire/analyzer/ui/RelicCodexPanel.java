package com.slayspire.analyzer.ui;

import com.slayspire.analyzer.database.RelicDAO;
import com.slayspire.analyzer.models.Relic;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RelicCodexPanel extends JPanel {
    private RelicDAO relicDAO;
    private RelicTableModel tableModel;
    private JTable relicTable;
    private JComboBox<String> poolFilter;
    private JComboBox<String> rarityFilter;
    private JTextField searchField;
    private JLabel statusLabel;
    private JTextArea detailArea;
    private List<Relic> currentRelics;

    public RelicCodexPanel(RelicDAO relicDAO) {
        this.relicDAO = relicDAO;
        this.currentRelics = new ArrayList<>();
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        loadFilters();
        refreshData();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        searchField = new JTextField(15);
        searchField.addActionListener(e -> refreshData());
        topPanel.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> refreshData());
        topPanel.add(searchBtn);

        topPanel.add(new JLabel("Pool:"));
        poolFilter = new JComboBox<>();
        poolFilter.addActionListener(e -> refreshData());
        topPanel.add(poolFilter);

        topPanel.add(new JLabel("Rarity:"));
        rarityFilter = new JComboBox<>();
        rarityFilter.addActionListener(e -> refreshData());
        topPanel.add(rarityFilter);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> clearFilters());
        topPanel.add(clearBtn);

        statusLabel = new JLabel(" ");
        topPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new RelicTableModel();
        relicTable = new JTable(tableModel);
        relicTable.setAutoCreateRowSorter(true);
        relicTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showRelicDetail();
        });
        JScrollPane scrollPane = new JScrollPane(relicTable);
        add(scrollPane, BorderLayout.CENTER);

        detailArea = new JTextArea(4, 30);
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Relic Details"));
        bottomPanel.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        bottomPanel.setPreferredSize(new Dimension(0, 120));
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadFilters() {
        try {
            poolFilter.addItem("All");
            for (String p : relicDAO.getAllPools()) {
                poolFilter.addItem(capitalize(p));
            }
            rarityFilter.addItem("All");
            for (String r : relicDAO.getAllRelicRarities()) {
                rarityFilter.addItem(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshData() {
        try {
            String searchText = searchField.getText().trim();
            String pool = getFilterValue(poolFilter);
            String rarity = getFilterValue(rarityFilter);

            List<Relic> relics;
            if (!searchText.isEmpty()) {
                relics = relicDAO.searchRelics(searchText);
                if (!pool.equals("All")) {
                    relics = relics.stream()
                            .filter(r -> r.getPool().equalsIgnoreCase(pool)).toList();
                }
                if (!rarity.equals("All")) {
                    relics = relics.stream()
                            .filter(r -> r.getRarity().equals(rarity)).toList();
                }
            } else if (!pool.equals("All") || !rarity.equals("All")) {
                relics = relicDAO.getFilteredRelics(
                        pool.equals("All") ? null : pool.toLowerCase(),
                        rarity.equals("All") ? null : rarity);
            } else {
                relics = relicDAO.getAllRelics();
            }

            currentRelics = relics;
            tableModel.setRelics(relics);
            statusLabel.setText(relics.size() + " relics found");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFilters() {
        poolFilter.setSelectedIndex(0);
        rarityFilter.setSelectedIndex(0);
        searchField.setText("");
        refreshData();
    }

    private String getFilterValue(JComboBox<String> box) {
        Object selected = box.getSelectedItem();
        return selected == null ? "All" : selected.toString();
    }

    private void showRelicDetail() {
        int row = relicTable.getSelectedRow();
        if (row >= 0 && row < currentRelics.size()) {
            Relic r = currentRelics.get(row);
            String desc = r.getDescription() != null ? r.getDescription() : "No description";
            String flavor = r.getFlavor() != null ? "\n\n\"" + r.getFlavor() + "\"" : "";
            String price = r.getMerchantPrice() != null && !r.getMerchantPrice().isEmpty()
                    ? "\nMerchant price: " + r.getMerchantPrice() : "";
            detailArea.setText(desc.replace("[gold]", "").replace("[/gold]", "")
                    .replace("[blue]", "").replace("[/blue]", "") + flavor + price);
            detailArea.setCaretPosition(0);
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static class RelicTableModel extends AbstractTableModel {
        private List<Relic> relics = new ArrayList<>();
        private final String[] columns = {"#", "Name", "Pool", "Rarity"};

        void setRelics(List<Relic> relics) {
            this.relics = new ArrayList<>(relics);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return relics.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Relic r = relics.get(row);
            return switch (col) {
                case 0 -> row + 1;
                case 1 -> r.getName();
                case 2 -> r.getPool();
                case 3 -> r.getRarity();
                default -> "";
            };
        }
    }
}
