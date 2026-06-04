package com.slayspire.analyzer.ui;

import com.slayspire.analyzer.database.*;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MainFrame extends JFrame {
    private CardDAO cardDAO;
    private RelicDAO relicDAO;
    private OddsCalculator oddsCalculator;
    private SearchService searchService;
    private JTabbedPane tabbedPane;
    private CardCodexPanel cardCodexPanel;
    private RelicCodexPanel relicCodexPanel;
    private StrategyPanel strategyPanel;
    private OddsPanel oddsPanel;
    private BuildPanel buildPanel;

    public MainFrame() throws SQLException {
        super("Slay the Spire 2 - Strategy Analyzer");
        cardDAO = new CardDAO();
        relicDAO = new RelicDAO();
        oddsCalculator = new OddsCalculator();
        searchService = new SearchService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200, 800));

        initMenuBar();
        initComponents();

        pack();
        setLocationRelativeTo(null);
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            DatabaseConnection.close();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu viewMenu = new JMenu("View");
        JMenuItem codexItem = new JMenuItem("Card Codex");
        codexItem.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        viewMenu.add(codexItem);
        JMenuItem relicItem = new JMenuItem("Relic Codex");
        relicItem.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        viewMenu.add(relicItem);
        JMenuItem buildItem = new JMenuItem("Strategy Builder");
        buildItem.addActionListener(e -> tabbedPane.setSelectedIndex(2));
        viewMenu.add(buildItem);
        JMenuItem oddsItem = new JMenuItem("Odds Calculator");
        oddsItem.addActionListener(e -> tabbedPane.setSelectedIndex(3));
        viewMenu.add(oddsItem);
        JMenuItem savesItem = new JMenuItem("Build Manager");
        savesItem.addActionListener(e -> tabbedPane.setSelectedIndex(4));
        viewMenu.add(savesItem);
        menuBar.add(viewMenu);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Slay the Spire 2 Strategy Analyzer\n" +
                "Database-driven strategy builder for STS2\n" +
                "Created by Lucas Leung",
                "About", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 13));

        cardCodexPanel = new CardCodexPanel(cardDAO, searchService);
        tabbedPane.addTab("Card Codex", cardCodexPanel);

        relicCodexPanel = new RelicCodexPanel(relicDAO);
        tabbedPane.addTab("Relic Codex", relicCodexPanel);

        strategyPanel = new StrategyPanel(cardDAO, relicDAO, oddsCalculator);
        tabbedPane.addTab("Strategy Builder", strategyPanel);

        oddsPanel = new OddsPanel(cardDAO, oddsCalculator);
        tabbedPane.addTab("Odds Calculator", oddsPanel);

        buildPanel = new BuildPanel(strategyPanel);
        tabbedPane.addTab("Build Manager", buildPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }
}
