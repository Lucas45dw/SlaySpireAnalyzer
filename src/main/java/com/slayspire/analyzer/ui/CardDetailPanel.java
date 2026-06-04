package com.slayspire.analyzer.ui;

import com.slayspire.analyzer.models.Card;
import javax.swing.*;
import java.awt.*;

public class CardDetailPanel extends JPanel {
    private JLabel nameLabel;
    private JLabel costLabel;
    private JLabel typeLabel;
    private JLabel rarityLabel;
    private JLabel characterLabel;
    private JTextArea descriptionArea;
    private JLabel upgradeHeader;
    private JTextArea upgradeArea;
    private JLabel noCardLabel;

    public CardDetailPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Card Details"));
        setPreferredSize(new Dimension(350, 0));
        initComponents();
    }

    private void initComponents() {
        noCardLabel = new JLabel("Select a card to view details");
        noCardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(noCardLabel);

        nameLabel = new JLabel(" ");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(nameLabel);

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 2));
        infoPanel.add(new JLabel("Cost:"));
        costLabel = new JLabel(" ");
        infoPanel.add(costLabel);
        infoPanel.add(new JLabel("Type:"));
        typeLabel = new JLabel(" ");
        infoPanel.add(typeLabel);
        infoPanel.add(new JLabel("Rarity:"));
        rarityLabel = new JLabel(" ");
        infoPanel.add(rarityLabel);
        infoPanel.add(new JLabel("Character:"));
        characterLabel = new JLabel(" ");
        infoPanel.add(characterLabel);
        add(infoPanel);

        add(Box.createVerticalStrut(10));
        JLabel descHeader = new JLabel("Description:");
        descHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        descHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        add(descHeader);

        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        add(new JScrollPane(descriptionArea));

        add(Box.createVerticalStrut(10));
        upgradeHeader = new JLabel("Upgraded:");
        upgradeHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        upgradeHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        add(upgradeHeader);

        upgradeArea = new JTextArea(5, 30);
        upgradeArea.setEditable(false);
        upgradeArea.setLineWrap(true);
        upgradeArea.setWrapStyleWord(true);
        upgradeArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        upgradeArea.setBackground(new Color(230, 255, 230));
        add(new JScrollPane(upgradeArea));

        add(Box.createVerticalGlue());
        hideDetails();
    }

    public void showCard(Card card) {
        noCardLabel.setVisible(false);
        nameLabel.setText(card.getName());
        costLabel.setText(card.getFormattedCost());
        typeLabel.setText(card.getType());
        rarityLabel.setText(card.getRarity());
        characterLabel.setText(capitalize(card.getColor()));

        String desc = card.getDescription();
        if (desc != null) {
            descriptionArea.setText(desc.replace("[gold]", "").replace("[/gold]", "")
                    .replace("[energy:1]", "[E]").replace("[energy:2]", "[EE]")
                    .replace("[energy:3]", "[EEE]"));
        } else {
            descriptionArea.setText("");
        }
        descriptionArea.setCaretPosition(0);

        if (card.hasUpgrade()) {
            upgradeHeader.setVisible(true);
            upgradeArea.setVisible(true);
            String ugDesc = card.getUpgradeDescription();
            if (ugDesc != null) {
                upgradeArea.setText("Stats changed: " + card.getUpgrade() + "\n\n" +
                        ugDesc.replace("[gold]", "").replace("[/gold]", "")
                                .replace("[energy:1]", "[E]").replace("[energy:2]", "[EE]")
                                .replace("[energy:3]", "[EEE]"));
            } else {
                upgradeArea.setText("Stats changed: " + card.getUpgrade());
            }
            upgradeArea.setCaretPosition(0);
        } else {
            upgradeHeader.setVisible(false);
            upgradeArea.setVisible(false);
        }

        nameLabel.setVisible(true);
        revalidate();
        repaint();
    }

    public void clear() {
        hideDetails();
        revalidate();
        repaint();
    }

    private void hideDetails() {
        noCardLabel.setVisible(true);
        nameLabel.setVisible(false);
        costLabel.setText(" ");
        typeLabel.setText(" ");
        rarityLabel.setText(" ");
        characterLabel.setText(" ");
        descriptionArea.setText("");
        upgradeHeader.setVisible(false);
        upgradeArea.setVisible(false);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
