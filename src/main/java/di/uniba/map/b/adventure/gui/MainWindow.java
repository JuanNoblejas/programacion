package di.uniba.map.b.adventure.gui;

import di.uniba.map.b.adventure.core.GameEngine;
import di.uniba.map.b.adventure.entities.Contenitore;
import di.uniba.map.b.adventure.entities.Item;
import di.uniba.map.b.adventure.entities.Stanza;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainWindow extends JFrame implements GameEngine.EngineListener {
    private GameEngine engine;
    
    private JTextArea txtNarrativa;
    private JTextField txtComando;
    private JProgressBar progressOxigeno;
    private JLabel lblHabitacion;
    private DefaultListModel<String> modelInventario;
    private JList<String> listInventario;
    private MapPanel mapPanel;
    private JButton btnPausa;
    private JPanel pnlOverlayPausa;

    public MainWindow() {
        setTitle("Odisea en la Estacion Espacial");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Tema Oscuro Sci-Fi
        getContentPane().setBackground(new Color(20, 20, 25));
        
        initUI();
        
        engine = new GameEngine(this);
        engine.iniciarJuego();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // Panel Norte: Oxigeno, Habitacion y Boton de Pausa
        JPanel pnlNorte = new JPanel(new BorderLayout(5, 5));
        pnlNorte.setBackground(new Color(30, 30, 40));
        pnlNorte.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        lblHabitacion = new JLabel("Habitacion: ---");
        lblHabitacion.setForeground(new Color(150, 200, 255));
        lblHabitacion.setFont(new Font("Consolas", Font.BOLD, 16));
        
        progressOxigeno = new JProgressBar(0, 100);
        progressOxigeno.setValue(100);
        progressOxigeno.setStringPainted(true);
        progressOxigeno.setForeground(new Color(0, 200, 100));
        progressOxigeno.setBackground(Color.DARK_GRAY);

        // Boton de Pausa
        btnPausa = new JButton("\u23F8 Pausa");
        btnPausa.setBackground(new Color(80, 80, 100));
        btnPausa.setForeground(Color.WHITE);
        btnPausa.setFont(new Font("Consolas", Font.BOLD, 12));
        btnPausa.setFocusPainted(false);
        btnPausa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPausa.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 130)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        btnPausa.addActionListener(e -> togglePausa());

        JPanel pnlOxigeno = new JPanel(new BorderLayout(5, 0));
        pnlOxigeno.setOpaque(false);
        pnlOxigeno.add(new JLabel("Oxigeno: ") {{
            setForeground(new Color(150, 200, 255));
            setFont(new Font("Consolas", Font.PLAIN, 12));
        }}, BorderLayout.WEST);
        pnlOxigeno.add(progressOxigeno, BorderLayout.CENTER);
        pnlOxigeno.add(btnPausa, BorderLayout.EAST);

        pnlNorte.add(lblHabitacion, BorderLayout.WEST);
        pnlNorte.add(pnlOxigeno, BorderLayout.CENTER);
        
        add(pnlNorte, BorderLayout.NORTH);

        // Panel Centro: Narrativa (con overlay de pausa)
        JLayeredPane layeredCenter = new JLayeredPane();
        layeredCenter.setLayout(new OverlayLayout(layeredCenter));

        txtNarrativa = new JTextArea();
        txtNarrativa.setEditable(false);
        txtNarrativa.setLineWrap(true);
        txtNarrativa.setWrapStyleWord(true);
        txtNarrativa.setBackground(new Color(10, 10, 15));
        txtNarrativa.setForeground(new Color(0, 255, 0));
        txtNarrativa.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        JScrollPane scrollNarrativa = new JScrollPane(txtNarrativa);
        scrollNarrativa.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0)));

        // Overlay de pausa (oculto por defecto)
        pnlOverlayPausa = new JPanel(new GridBagLayout());
        pnlOverlayPausa.setBackground(new Color(0, 0, 0, 180));
        pnlOverlayPausa.setVisible(false);
        JLabel lblPausa = new JLabel("JUEGO EN PAUSA");
        lblPausa.setFont(new Font("Consolas", Font.BOLD, 28));
        lblPausa.setForeground(new Color(255, 200, 50));
        pnlOverlayPausa.add(lblPausa);

        layeredCenter.add(scrollNarrativa);
        layeredCenter.add(pnlOverlayPausa);

        add(layeredCenter, BorderLayout.CENTER);

        // =====================================================================
        // Panel Este: Mapa + Inventario (apilados verticalmente)
        // =====================================================================
        JPanel pnlEste = new JPanel();
        pnlEste.setLayout(new BoxLayout(pnlEste, BoxLayout.Y_AXIS));
        pnlEste.setBackground(new Color(20, 20, 25));

        // --- Mapa ---
        mapPanel = new MapPanel();
        mapPanel.setMaximumSize(new Dimension(280, 220));
        mapPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlEste.add(mapPanel);
        pnlEste.add(Box.createVerticalStrut(8));

        // --- Inventario ---
        JLabel lblInv = new JLabel("  INVENTARIO");
        lblInv.setForeground(Color.CYAN);
        lblInv.setFont(new Font("Consolas", Font.BOLD, 12));
        lblInv.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlEste.add(lblInv);
        
        modelInventario = new DefaultListModel<>();
        listInventario = new JList<>(modelInventario);
        listInventario.setBackground(new Color(30, 30, 40));
        listInventario.setForeground(Color.WHITE);
        listInventario.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        JScrollPane scrollInv = new JScrollPane(listInventario);
        scrollInv.setPreferredSize(new Dimension(260, 120));
        scrollInv.setMaximumSize(new Dimension(280, 200));
        scrollInv.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlEste.add(scrollInv);
        
        add(pnlEste, BorderLayout.EAST);

        // =====================================================================
        // Panel Sur: Direcciones + Botones de accion + Campo de comando
        // =====================================================================
        JPanel pnlSur = new JPanel(new BorderLayout(5, 5));
        pnlSur.setBackground(new Color(30, 30, 40));
        pnlSur.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // --- Botones de direccion (izquierda) ---
        JPanel pnlDirecciones = new JPanel(new GridLayout(3, 3, 2, 2));
        pnlDirecciones.setBackground(new Color(30, 30, 40));
        pnlDirecciones.setPreferredSize(new Dimension(180, 100));
        JButton btnNorte = createDirButton("\u2191 Norte", "norte");
        JButton btnSur   = createDirButton("\u2193 Sur", "sur");
        JButton btnEste  = createDirButton("Este \u2192", "este");
        JButton btnOeste = createDirButton("\u2190 Oeste", "oeste");
        
        pnlDirecciones.add(new JLabel()); pnlDirecciones.add(btnNorte); pnlDirecciones.add(new JLabel());
        pnlDirecciones.add(btnOeste); pnlDirecciones.add(new JLabel()); pnlDirecciones.add(btnEste);
        pnlDirecciones.add(new JLabel()); pnlDirecciones.add(btnSur); pnlDirecciones.add(new JLabel());
        
        pnlSur.add(pnlDirecciones, BorderLayout.WEST);

        // --- Botones de acciones rapidas (centro) ---
        JPanel pnlAcciones = new JPanel(new GridLayout(2, 4, 4, 4));
        pnlAcciones.setBackground(new Color(30, 30, 40));
        pnlAcciones.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                " Acciones ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Consolas", Font.BOLD, 11),
                new Color(0, 180, 220)
            ),
            BorderFactory.createEmptyBorder(2, 4, 4, 4)
        ));

        pnlAcciones.add(createActionButton("\uD83D\uDC41 Mirar",     "mirar",     new Color(60, 100, 140)));
        pnlAcciones.add(createActionButton("\u270B Tomar",            "tomar",     new Color(60, 120, 60)));
        pnlAcciones.add(createActionButton("\u2699 Usar",             "usar",      new Color(140, 120, 40)));
        pnlAcciones.add(createActionButton("\uD83C\uDF92 Inventario", "inv",       new Color(100, 60, 140)));
        pnlAcciones.add(createActionButton("\uD83D\uDCBB Hackear",   "hackear",   new Color(140, 50, 50)));
        pnlAcciones.add(createActionButton("\uD83D\uDCBE Guardar",   "guardar",   new Color(50, 100, 100)));
        pnlAcciones.add(createActionButton("\uD83D\uDCC2 Cargar",    "cargar",    new Color(50, 100, 100)));
        pnlAcciones.add(createActionButton("\u2753 Ayuda",            "ayuda",     new Color(80, 80, 100)));

        pnlSur.add(pnlAcciones, BorderLayout.CENTER);

        // --- Campo de comandos (abajo) ---
        JPanel pnlComando = new JPanel(new BorderLayout(5, 0));
        pnlComando.setBackground(new Color(30, 30, 40));
        pnlComando.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        
        JLabel lblCmd = new JLabel(" > ");
        lblCmd.setForeground(new Color(0, 255, 0));
        lblCmd.setFont(new Font("Consolas", Font.BOLD, 14));

        txtComando = new JTextField();
        txtComando.setBackground(Color.BLACK);
        txtComando.setForeground(Color.GREEN);
        txtComando.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtComando.setCaretColor(Color.GREEN);
        txtComando.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 0)),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        txtComando.addActionListener((ActionEvent e) -> {
            String input = txtComando.getText();
            txtComando.setText("");
            if (!input.trim().isEmpty()) {
                onMessage("> " + input);
                engine.procesarComando(input);
            }
        });
        
        pnlComando.add(lblCmd, BorderLayout.WEST);
        pnlComando.add(txtComando, BorderLayout.CENTER);
        
        pnlSur.add(pnlComando, BorderLayout.SOUTH);
        
        add(pnlSur, BorderLayout.SOUTH);
    }

    /**
     * Alterna entre pausa y reanudacion del juego.
     */
    private void togglePausa() {
        if (engine.isPausado()) {
            // Reanudar
            engine.reanudarTimer();
            btnPausa.setText("\u23F8 Pausa");
            btnPausa.setBackground(new Color(80, 80, 100));
            txtComando.setEnabled(true);
            pnlOverlayPausa.setVisible(false);
            onMessage("[Sistema] Juego reanudado.");
        } else {
            // Pausar
            engine.pausarTimer();
            btnPausa.setText("\u25B6 Reanudar");
            btnPausa.setBackground(new Color(50, 130, 50));
            txtComando.setEnabled(false);
            pnlOverlayPausa.setVisible(true);
            onMessage("[Sistema] Juego en pausa.");
        }
    }

    /**
     * Crea un boton de direccion con estilo sci-fi.
     */
    private JButton createDirButton(String text, String dir) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(50, 50, 70));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Consolas", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            onMessage("> ir " + dir);
            engine.procesarComando("ir " + dir);
        });
        return btn;
    }

    /**
     * Crea un boton de accion rapida con color personalizado.
     * Para comandos que necesitan argumento (tomar, usar), se abre un dialogo.
     * El timer se pausa automaticamente durante los dialogos.
     */
    private JButton createActionButton(String text, String comando, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            // Comandos que necesitan un argumento adicional
            if (comando.equals("tomar") || comando.equals("usar")) {
                // Pausar el timer mientras el dialogo esta abierto
                engine.pausarTimer();
                String arg = JOptionPane.showInputDialog(
                    this,
                    "Escribe el nombre del objeto para '" + comando + "':",
                    comando.substring(0, 1).toUpperCase() + comando.substring(1),
                    JOptionPane.QUESTION_MESSAGE
                );
                // Reanudar el timer al cerrar el dialogo
                engine.reanudarTimer();
                if (arg != null && !arg.trim().isEmpty()) {
                    String full = comando + " " + arg.trim();
                    onMessage("> " + full);
                    engine.procesarComando(full);
                }
            } else {
                onMessage("> " + comando);
                engine.procesarComando(comando);
            }
        });
        return btn;
    }

    @Override
    public void onMessage(String msg) {
        txtNarrativa.append(msg + "\n");
        txtNarrativa.setCaretPosition(txtNarrativa.getDocument().getLength());
    }

    @Override
    public void onOxygenUpdate(int oxigeno) {
        progressOxigeno.setValue(oxigeno);
        if (oxigeno < 20) {
            progressOxigeno.setForeground(Color.RED);
        } else if (oxigeno < 50) {
            progressOxigeno.setForeground(Color.ORANGE);
        } else {
            progressOxigeno.setForeground(new Color(0, 200, 100));
        }
    }

    @Override
    public void onGameOver(boolean win) {
        txtComando.setEnabled(false);
        // Cerrar la ventana principal y abrir la pantalla de Game Over
        dispose();
        SwingUtilities.invokeLater(() -> {
            new GameOverWindow(win).setVisible(true);
        });
    }

    @Override
    public void onRoomChange(Stanza room) {
        lblHabitacion.setText("Sector: " + room.getNombre());
        // Actualizar el mapa visual con la nueva habitacion
        mapPanel.actualizarHabitacion(room.getId());
    }

    @Override
    public void onInventoryChange(Contenitore<Item> inventory) {
        modelInventario.clear();
        for (Item item : inventory.getElementos()) {
            modelInventario.addElement(item.getNombre());
        }
    }

    /**
     * Devuelve el panel del mapa para poder acceder a las habitaciones visitadas
     * desde el sistema de guardado/carga.
     */
    public MapPanel getMapPanel() {
        return mapPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}
