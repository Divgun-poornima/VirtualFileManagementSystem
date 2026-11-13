package com.vfms;

import com.vfms.ui.MainFrame;
import com.vfms.ui.theme.UITheme;

import javax.swing.SwingUtilities;

/**
 * Main application entry point for the Virtual File Management System (VFMS).
 * College NSP Minor Project.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize modern Look and Feel
        UITheme.initializeTheme();

        // Launch GUI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Error launching Virtual File Management System:\n" + e.getMessage(),
                    "Fatal Launch Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}

