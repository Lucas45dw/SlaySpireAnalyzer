package com.slayspire.analyzer;

import com.slayspire.analyzer.database.DatabaseConnection;
import com.slayspire.analyzer.ui.MainFrame;
import javax.swing.*;
import java.io.*;
import java.sql.SQLException;

public class Main {

    public static void printFileContents(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // printFileContents("builds/example.txt");

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
