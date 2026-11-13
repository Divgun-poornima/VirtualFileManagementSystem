package com.vfms.test;

import com.vfms.ui.MainFrame;
import com.vfms.ui.theme.UITheme;

import javax.swing.SwingUtilities;

/**
 * Smoke test verifying GUI components, theme, and frame construct without throwing runtime exceptions.
 */
public class GuiSmokeTest {
    public static void main(String[] args) throws Exception {
        System.out.println(">>> Running GUI Smoke Test <<<");
        UITheme.initializeTheme();

        SwingUtilities.invokeAndWait(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.pack();
                System.out.println("  [PASS] MainFrame constructed, packed, and laid out successfully.");
                System.out.println("  [INFO] Title: " + frame.getTitle());
                System.out.println("  [INFO] Size: " + frame.getWidth() + "x" + frame.getHeight());
                frame.dispose();
            } catch (Exception e) {
                System.err.println("  [FAIL] GUI construction error: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        });

        System.out.println(">>> GUI Smoke Test Completed Successfully! <<<");
    }
}

