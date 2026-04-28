package di.uniba.map.b.adventure.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Ventana de Game Over / Mision Cumplida.
 * Se muestra cuando el juego termina (por falta de oxigeno o por victoria).
 */
public class GameOverWindow extends JFrame {

    public GameOverWindow(boolean win) {
        setTitle(win ? "Mision Cumplida" : "Game Over");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Fondo oscuro sci-fi
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                // Degradado de fondo
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

        // Titulo grande
        JLabel lblTitulo = new JLabel(win ? "MISION CUMPLIDA" : "GAME OVER");
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitulo.setFont(new Font("Consolas", Font.BOLD, 48));
        lblTitulo.setForeground(win ? new Color(0, 255, 150) : new Color(255, 50, 50));

        // Mensaje narrativo
        String mensajeTexto;
        if (win) {
            mensajeTexto = "<html><center>Has logrado reparar el modulo de escape<br>"
                    + "y abandonar la estacion espacial.<br><br>"
                    + "La Tierra te espera, astronauta.</center></html>";
        } else {
            mensajeTexto = "<html><center>El oxigeno se ha agotado...<br>"
                    + "La estacion espacial sera tu tumba eterna.<br><br>"
                    + "Nadie escuchara tu ultimo aliento en el vacio.</center></html>";
        }
        JLabel lblMensaje = new JLabel(mensajeTexto);
        lblMensaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblMensaje.setFont(new Font("Consolas", Font.PLAIN, 14));
        lblMensaje.setForeground(new Color(180, 200, 220));
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);

        // Panel de botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        pnlBotones.setOpaque(false);

        JButton btnNuevoJuego = createStyledButton("Nuevo Juego", new Color(0, 120, 80));
        btnNuevoJuego.addActionListener((ActionEvent e) -> {
            dispose();
            SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
        });

        JButton btnSalir = createStyledButton("Salir", new Color(120, 30, 30));
        btnSalir.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });

        pnlBotones.add(btnNuevoJuego);
        pnlBotones.add(btnSalir);

        // Montar layout
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(lblTitulo);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(lblMensaje);
        mainPanel.add(Box.createVerticalStrut(40));
        mainPanel.add(pnlBotones);
        mainPanel.add(Box.createVerticalGlue());

        setContentPane(mainPanel);
    }

    /**
     * Crea un boton estilizado para la pantalla de Game Over.
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
