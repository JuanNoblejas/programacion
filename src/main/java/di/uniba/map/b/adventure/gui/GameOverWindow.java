package di.uniba.map.b.adventure.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Game Over / Mission Complete window.
 * Shown when the game ends (out of oxygen or victory).
 */
public class GameOverWindow extends JFrame {

    public GameOverWindow(boolean win) {
        setTitle(win ? "Mission Complete" : "Game Over");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Dark sci-fi background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Background gradient
                GradientPaint gp;
                if (win) {
                    gp = new GradientPaint(0, 0, new Color(10, 30, 50),
                            0, getHeight(), new Color(0, 80, 60));
                } else {
                    gp = new GradientPaint(0, 0, new Color(40, 5, 5),
                            0, getHeight(), new Color(10, 10, 15));
                }
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Large title
        JLabel lblTitle = new JLabel(win ? "MISSION COMPLETE" : "GAME OVER");
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setFont(new Font("Consolas", Font.BOLD, 48));
        lblTitle.setForeground(win ? new Color(0, 255, 150) : new Color(255, 50, 50));

        // Narrative message
        String messageText;
        if (win) {
            messageText = "<html><center>You managed to repair the escape module<br>"
                    + "and flee the space station.<br><br>"
                    + "Earth awaits you, astronaut.</center></html>";
        } else {
            messageText = "<html><center>The oxygen has run out...<br>"
                    + "The space station will be your eternal tomb.<br><br>"
                    + "No one will hear your last breath in the void.</center></html>";
        }
        JLabel lblMessage = new JLabel(messageText);
        lblMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMessage.setFont(new Font("Consolas", Font.PLAIN, 14));
        lblMessage.setForeground(new Color(180, 200, 220));
        lblMessage.setHorizontalAlignment(SwingConstants.CENTER);

        // Button panel
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pnlButtons.setOpaque(false);

        JButton btnNewGame = createStyledButton("New Game", new Color(0, 120, 80));
        btnNewGame.addActionListener((ActionEvent e) -> {
            dispose();
            SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
        });

        JButton btnQuit = createStyledButton("Quit", new Color(120, 30, 30));
        btnQuit.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });

        pnlButtons.add(btnNewGame);
        pnlButtons.add(btnQuit);

        // Assemble layout
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(lblMessage);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(pnlButtons);
        mainPanel.add(Box.createVerticalGlue());

        setContentPane(mainPanel);
    }

    /**
     * Creates a styled button for the Game Over screen.
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Consolas", Font.BOLD, 16));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.brighter(), 2),
                BorderFactory.createEmptyBorder(10, 30, 10, 30)
        ));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }
}
