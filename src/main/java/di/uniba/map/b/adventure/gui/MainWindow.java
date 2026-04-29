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
    
    private JTextArea txtNarrative;
    private JTextField txtCommand;
    private JProgressBar progressOxygen;
    private JLabel lblRoom;
    private DefaultListModel<String> modelInventory;
    private JList<String> listInventory;
    private MapPanel mapPanel;
    private JButton btnPause;
    private JPanel pnlOverlayPause;

    public MainWindow() {
        setTitle("Space Station Odyssey");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Dark Sci-Fi theme
        getContentPane().setBackground(new Color(20, 20, 25));
        
        initUI();
        
        engine = new GameEngine(this);
        engine.iniciarJuego();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // North panel: Oxygen, Room and Pause button
        JPanel pnlNorth = new JPanel(new BorderLayout(5, 5));
        pnlNorth.setBackground(new Color(30, 30, 40));
        pnlNorth.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        lblRoom = new JLabel("Room: ---");
        lblRoom.setForeground(new Color(150, 200, 255));
        lblRoom.setFont(new Font("Consolas", Font.BOLD, 16));
        
        progressOxygen = new JProgressBar(0, 300);
        progressOxygen.setValue(300);
        progressOxygen.setStringPainted(true);
        progressOxygen.setForeground(new Color(0, 200, 100));
        progressOxygen.setBackground(Color.DARK_GRAY);

        // Pause button
        btnPause = new JButton("\u23F8 Pause");
        btnPause.setBackground(new Color(80, 80, 100));
        btnPause.setForeground(Color.WHITE);
        btnPause.setFont(new Font("Consolas", Font.BOLD, 12));
        btnPause.setFocusPainted(false);
        btnPause.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPause.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 130)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        btnPause.addActionListener(e -> togglePause());

        JPanel pnlOxygen = new JPanel(new BorderLayout(5, 0));
        pnlOxygen.setOpaque(false);
        pnlOxygen.add(new JLabel("Oxygen: ") {{
            setForeground(new Color(150, 200, 255));
            setFont(new Font("Consolas", Font.PLAIN, 12));
        }}, BorderLayout.WEST);
        pnlOxygen.add(progressOxygen, BorderLayout.CENTER);
        pnlOxygen.add(btnPause, BorderLayout.EAST);

        pnlNorth.add(lblRoom, BorderLayout.WEST);
        pnlNorth.add(pnlOxygen, BorderLayout.CENTER);
        
        add(pnlNorth, BorderLayout.NORTH);

        // Center panel: Narrative (with pause overlay)
        JLayeredPane layeredCenter = new JLayeredPane();
        layeredCenter.setLayout(new OverlayLayout(layeredCenter));

        txtNarrative = new JTextArea();
        txtNarrative.setEditable(false);
        txtNarrative.setLineWrap(true);
        txtNarrative.setWrapStyleWord(true);
        txtNarrative.setBackground(new Color(10, 10, 15));
        txtNarrative.setForeground(new Color(0, 255, 0));
        txtNarrative.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        JScrollPane scrollNarrative = new JScrollPane(txtNarrative);
        scrollNarrative.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0)));

        // Pause overlay (hidden by default)
        pnlOverlayPause = new JPanel(new GridBagLayout());
        pnlOverlayPause.setBackground(new Color(0, 0, 0, 180));
        pnlOverlayPause.setVisible(false);
        JLabel lblPause = new JLabel("GAME PAUSED");
        lblPause.setFont(new Font("Consolas", Font.BOLD, 28));
        lblPause.setForeground(new Color(255, 200, 50));
        pnlOverlayPause.add(lblPause);

        layeredCenter.add(scrollNarrative);
        layeredCenter.add(pnlOverlayPause);

        add(layeredCenter, BorderLayout.CENTER);

        // =====================================================================
        // East panel: Map + Inventory (stacked vertically)
        // =====================================================================
        JPanel pnlEast = new JPanel();
        pnlEast.setLayout(new BoxLayout(pnlEast, BoxLayout.Y_AXIS));
        pnlEast.setBackground(new Color(20, 20, 25));

        // --- Map ---
        mapPanel = new MapPanel();
        mapPanel.setMaximumSize(new Dimension(280, 220));
        mapPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlEast.add(mapPanel);
        pnlEast.add(Box.createVerticalStrut(8));

        // --- Inventory ---
        JLabel lblInv = new JLabel("  INVENTORY");
        lblInv.setForeground(Color.CYAN);
        lblInv.setFont(new Font("Consolas", Font.BOLD, 12));
        lblInv.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlEast.add(lblInv);
        
        modelInventory = new DefaultListModel<>();
        listInventory = new JList<>(modelInventory);
        listInventory.setBackground(new Color(30, 30, 40));
        listInventory.setForeground(Color.WHITE);
        listInventory.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        JScrollPane scrollInv = new JScrollPane(listInventory);
        scrollInv.setPreferredSize(new Dimension(260, 120));
        scrollInv.setMaximumSize(new Dimension(280, 200));
        scrollInv.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlEast.add(scrollInv);
        
        add(pnlEast, BorderLayout.EAST);

        // =====================================================================
        // South panel: Directions + Action buttons + Command field
        // =====================================================================
        JPanel pnlSouth = new JPanel(new BorderLayout(5, 5));
        pnlSouth.setBackground(new Color(30, 30, 40));
        pnlSouth.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        // --- Direction buttons (left) ---
        JPanel pnlDirections = new JPanel(new GridLayout(3, 3, 2, 2));
        pnlDirections.setBackground(new Color(30, 30, 40));
        pnlDirections.setPreferredSize(new Dimension(180, 100));
        JButton btnNorth = createDirButton("\u2191 North", "north");
        JButton btnSouthDir = createDirButton("\u2193 South", "south");
        JButton btnEast  = createDirButton("East \u2192", "east");
        JButton btnWest  = createDirButton("\u2190 West", "west");
        
        pnlDirections.add(new JLabel()); pnlDirections.add(btnNorth); pnlDirections.add(new JLabel());
        pnlDirections.add(btnWest); pnlDirections.add(new JLabel()); pnlDirections.add(btnEast);
        pnlDirections.add(new JLabel()); pnlDirections.add(btnSouthDir); pnlDirections.add(new JLabel());
        
        pnlSouth.add(pnlDirections, BorderLayout.WEST);

        // --- Quick action buttons (center) ---
        JPanel pnlActions = new JPanel(new GridLayout(2, 4, 4, 4));
        pnlActions.setBackground(new Color(30, 30, 40));
        pnlActions.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 80)),
                " Actions ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Consolas", Font.BOLD, 11),
                new Color(0, 180, 220)
            ),
            BorderFactory.createEmptyBorder(2, 4, 4, 4)
        ));

        pnlActions.add(createActionButton("\uD83D\uDC41 Look",      "look",   new Color(60, 100, 140)));
        pnlActions.add(createActionButton("\u270B Take",             "take",   new Color(60, 120, 60)));
        pnlActions.add(createActionButton("\u2699 Use",              "use",    new Color(140, 120, 40)));
        pnlActions.add(createActionButton("\uD83C\uDF92 Inventory",  "inv",    new Color(100, 60, 140)));
        pnlActions.add(createActionButton("\uD83D\uDCBB Hack",      "hack",   new Color(140, 50, 50)));
        pnlActions.add(createActionButton("\uD83D\uDCBE Save",      "save",   new Color(50, 100, 100)));
        pnlActions.add(createActionButton("\uD83D\uDCC2 Load",      "load",   new Color(50, 100, 100)));
        pnlActions.add(createActionButton("\u2753 Help",             "help",   new Color(80, 80, 100)));

        pnlSouth.add(pnlActions, BorderLayout.CENTER);

        // --- Command field (bottom) ---
        JPanel pnlCommand = new JPanel(new BorderLayout(5, 0));
        pnlCommand.setBackground(new Color(30, 30, 40));
        pnlCommand.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        
        JLabel lblCmd = new JLabel(" > ");
        lblCmd.setForeground(new Color(0, 255, 0));
        lblCmd.setFont(new Font("Consolas", Font.BOLD, 14));

        txtCommand = new JTextField();
        txtCommand.setBackground(Color.BLACK);
        txtCommand.setForeground(Color.GREEN);
        txtCommand.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtCommand.setCaretColor(Color.GREEN);
        txtCommand.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 0)),
            BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        txtCommand.addActionListener((ActionEvent e) -> {
            String input = txtCommand.getText();
            txtCommand.setText("");
            if (!input.trim().isEmpty()) {
                onMessage("> " + input);
                engine.procesarComando(input);
            }
        });
        
        pnlCommand.add(lblCmd, BorderLayout.WEST);
        pnlCommand.add(txtCommand, BorderLayout.CENTER);
        
        pnlSouth.add(pnlCommand, BorderLayout.SOUTH);
        
        add(pnlSouth, BorderLayout.SOUTH);
    }

    /**
     * Toggles between pause and resume.
     */
    private void togglePause() {
        if (engine.isPausado()) {
            // Resume
            engine.reanudarTimer();
            btnPause.setText("\u23F8 Pause");
            btnPause.setBackground(new Color(80, 80, 100));
            txtCommand.setEnabled(true);
            pnlOverlayPause.setVisible(false);
            onMessage("[System] Game resumed.");
        } else {
            // Pause
            engine.pausarTimer();
            btnPause.setText("\u25B6 Resume");
            btnPause.setBackground(new Color(50, 130, 50));
            txtCommand.setEnabled(false);
            pnlOverlayPause.setVisible(true);
            onMessage("[System] Game paused.");
        }
    }

    /**
     * Creates a sci-fi styled direction button.
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
            onMessage("> go " + dir);
            engine.procesarComando("go " + dir);
        });
        return btn;
    }

    /**
     * Creates a quick action button with custom color.
     * For commands that need an argument (take, use), a dialog opens.
     * The timer pauses automatically during dialogs.
     */
    private JButton createActionButton(String text, String command, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            // Commands that need an additional argument
            if (command.equals("take") || command.equals("use")) {
                // Pause timer while dialog is open
                engine.pausarTimer();
                String arg = JOptionPane.showInputDialog(
                    this,
                    "Enter the item name for '" + command + "':",
                    command.substring(0, 1).toUpperCase() + command.substring(1),
                    JOptionPane.QUESTION_MESSAGE
                );
                // Resume timer when dialog closes
                engine.reanudarTimer();
                if (arg != null && !arg.trim().isEmpty()) {
                    String full = command + " " + arg.trim();
                    onMessage("> " + full);
                    engine.procesarComando(full);
                }
            } else {
                onMessage("> " + command);
                engine.procesarComando(command);
            }
        });
        return btn;
    }

    @Override
    public void onMessage(String msg) {
        txtNarrative.append(msg + "\n");
        txtNarrative.setCaretPosition(txtNarrative.getDocument().getLength());
    }

    @Override
    public void onOxygenUpdate(int oxygen) {
        progressOxygen.setValue(oxygen);
        if (oxygen < 60) {
            progressOxygen.setForeground(Color.RED);
        } else if (oxygen < 150) {
            progressOxygen.setForeground(Color.ORANGE);
        } else {
            progressOxygen.setForeground(new Color(0, 200, 100));
        }
    }

    @Override
    public void onGameOver(boolean win) {
        txtCommand.setEnabled(false);
        // Close main window and open Game Over screen
        dispose();
        SwingUtilities.invokeLater(() -> {
            new GameOverWindow(win).setVisible(true);
        });
    }

    @Override
    public void onRoomChange(Stanza room) {
        lblRoom.setText("Sector: " + room.getNombre());
        // Update visual map with the new room
        mapPanel.actualizarHabitacion(room.getId());
    }

    @Override
    public void onInventoryChange(Contenitore<Item> inventory) {
        modelInventory.clear();
        for (Item item : inventory.getElementos()) {
            modelInventory.addElement(item.getNombre());
        }
    }

    /**
     * Returns the map panel so visited rooms can be accessed
     * from the save/load system.
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
