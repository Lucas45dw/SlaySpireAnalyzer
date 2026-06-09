package com.slayspire.analyzer.ui;

import com.slayspire.analyzer.database.BuildManager;
import com.slayspire.analyzer.models.Card;
import com.slayspire.analyzer.models.Relic;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class BuildPanel extends JPanel {
    private BuildManager buildManager;
    private StrategyPanel strategyPanel;
    private JTextField buildNameField;
    private DefaultListModel<String> buildListModel;
    private JList<String> buildList;
    private JLabel statusLabel;
    private JTextArea previewArea;

    public BuildPanel(StrategyPanel strategyPanel) {
        this.strategyPanel = strategyPanel;
        this.buildManager = new BuildManager();
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshBuildList();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Build Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        buildNameField = new JTextField(20);
        topPanel.add(buildNameField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        JButton loadFileBtn = new JButton("Load from File");
        loadFileBtn.addActionListener(e -> loadFromFile());
        topPanel.add(loadFileBtn, gbc);
        gbc.gridx = 3;
        JButton saveBtn = new JButton("Save Current Build");
        saveBtn.addActionListener(e -> saveBuild());
        topPanel.add(saveBtn, gbc);

        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Saved Builds"));
        buildListModel = new DefaultListModel<>();
        buildList = new JList<>(buildListModel);
        buildList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        leftPanel.add(new JScrollPane(buildList), BorderLayout.CENTER);

        JPanel leftButtons = new JPanel(new FlowLayout());
        JButton loadBtn = new JButton("Load Build");
        loadBtn.addActionListener(e -> loadBuild());
        leftButtons.add(loadBtn);
        JButton deleteBtn = new JButton("Delete Build");
        deleteBtn.addActionListener(e -> deleteBuild());
        leftButtons.add(deleteBtn);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshBuildList());
        leftButtons.add(refreshBtn);
        leftPanel.add(leftButtons, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Build Preview"));
        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        rightPanel.add(new JScrollPane(previewArea), BorderLayout.CENTER);

        buildList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = buildList.getSelectedValue();
                if (selected != null) {
                    try {
                        BuildManager.BuildData data = buildManager.loadBuild(selected);
                        StringBuilder preview = new StringBuilder();
                        preview.append("Build: ").append(data.name).append("\n");
                        preview.append("Cards:\n");
                        for (Card c : data.cards) {
                            preview.append("  - ").append(c.getName()).append(" (").append(c.getRarity()).append(")\n");
                        }
                        preview.append("Relics:\n");
                        for (Relic r : data.relics) {
                            preview.append("  - ").append(r.getName()).append(" (").append(r.getRarity()).append(")\n");
                        }
                        previewArea.setText(preview.toString());
                    } catch (IOException ex) {
                        previewArea.setText("Error loading preview");
                    }
                }
            }
        });

        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void saveBuild() {
        String name = buildNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a build name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Card> cards = strategyPanel.getStrategyBuilder().getSelectedCards();
        List<Relic> relics = strategyPanel.getStrategyBuilder().getSelectedRelics();

        if (cards.isEmpty() && relics.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot save an empty build. Add cards or relics first.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (buildManager.buildExists(name)) {
                String newName = JOptionPane.showInputDialog(this,
                        "File '" + name + "' already exists. Enter a different name:",
                        "File Exists", JOptionPane.WARNING_MESSAGE);
                if (newName == null || newName.trim().isEmpty()) {
                    statusLabel.setText("Save cancelled.");
                    return;
                }
                name = newName.trim();
            }
            buildManager.saveBuild(name, cards, relics);
            statusLabel.setText("Build '" + name + "' saved successfully!");
            refreshBuildList();
            buildNameField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving build: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadBuild() {
        String selected = buildList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a build to load.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BuildManager.BuildData data = buildManager.loadBuild(selected);
            strategyPanel.getStrategyBuilder().clear();
            for (Card c : data.cards) {
                strategyPanel.getStrategyBuilder().addCard(c);
            }
            for (Relic r : data.relics) {
                strategyPanel.getStrategyBuilder().addRelic(r);
            }
            strategyPanel.revalidate();
            strategyPanel.repaint();

            JTabbedPane parent = (JTabbedPane) SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);
            if (parent != null) {
                parent.setSelectedIndex(2);
            }

            statusLabel.setText("Build '" + selected + "' loaded!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading build: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBuild() {
        String selected = buildList.getSelectedValue();
        if (selected == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete build '" + selected + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            buildManager.deleteBuild(selected);
            statusLabel.setText("Build '" + selected + "' deleted.");
            refreshBuildList();
        }
    }

    private void loadFromFile() {
        String name = buildNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a build name to load.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            String content = buildManager.readRawFile(name);
            previewArea.setText(content);
            statusLabel.setText("Loaded file: " + name + ".txt");
        } catch (IOException e) {
            previewArea.setText("");
            statusLabel.setText("File not found: " + name + ".txt");
        }
    }

    private void refreshBuildList() {
        buildListModel.clear();
        for (String name : buildManager.listSavedBuilds()) {
            buildListModel.addElement(name);
        }
    }
}
