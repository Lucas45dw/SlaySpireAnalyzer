package com.slayspire.analyzer;

import com.slayspire.analyzer.database.DatabaseConnection;
import com.slayspire.analyzer.ui.MainFrame;
import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                DatabaseConnection.getConnection();
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to connect to database: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
