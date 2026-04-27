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

    public MainWindow() {
        setTitle("Odisea en la Estacion Espacial");
        setSize(800, 600);
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

        // Panel Norte: Oxigeno y Habitacion
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
        
        pnlNorte.add(lblHabitacion, BorderLayout.WEST);
        pnlNorte.add(new JLabel("Oxigeno: "), BorderLayout.CENTER);
        pnlNorte.add(progressOxigeno, BorderLayout.EAST);
        
        add(pnlNorte, BorderLayout.NORTH);

        // Panel Centro: Narrativa
        txtNarrativa = new JTextArea();
        txtNarrativa.setEditable(false);
        txtNarrativa.setLineWrap(true);
        txtNarrativa.setWrapStyleWord(true);
        txtNarrativa.setBackground(new Color(10, 10, 15));
        txtNarrativa.setForeground(new Color(0, 255, 0));
        txtNarrativa.setFont(new Font("Consolas", Font.PLAIN, 14));
        
        JScrollPane scrollNarrativa = new JScrollPane(txtNarrativa);
        scrollNarrativa.setBorder(BorderFactory.createLineBorder(new Color(0, 100, 0)));
        add(scrollNarrativa, BorderLayout.CENTER);

        // Panel Este: Inventario
        JPanel pnlEste = new JPanel(new BorderLayout());
        pnlEste.setBackground(new Color(20, 20, 25));
        JLabel lblInv = new JLabel("INVENTARIO");
        lblInv.setForeground(Color.CYAN);
        pnlEste.add(lblInv, BorderLayout.NORTH);
        
        modelInventario = new DefaultListModel<>();
        listInventario = new JList<>(modelInventario);
        listInventario.setBackground(new Color(30, 30, 40));
        listInventario.setForeground(Color.WHITE);
        
        JScrollPane scrollInv = new JScrollPane(listInventario);
        scrollInv.setPreferredSize(new Dimension(150, 0));
        pnlEste.add(scrollInv, BorderLayout.CENTER);
        
        add(pnlEste, BorderLayout.EAST);

        // Panel Sur: Controles y Entrada
        JPanel pnlSur = new JPanel(new BorderLayout(5, 5));
        pnlSur.setBackground(new Color(30, 30, 40));
        
        // Botones de direccion
        JPanel pnlDirecciones = new JPanel(new GridLayout(3, 3));
        pnlDirecciones.setBackground(new Color(30, 30, 40));
        JButton btnNorte = createButton("Norte", "norte");
        JButton btnSur = createButton("Sur", "sur");
        JButton btnEste = createButton("Este", "este");
        JButton btnOeste = createButton("Oeste", "oeste");
        
        pnlDirecciones.add(new JLabel()); pnlDirecciones.add(btnNorte); pnlDirecciones.add(new JLabel());
        pnlDirecciones.add(btnOeste); pnlDirecciones.add(new JLabel()); pnlDirecciones.add(btnEste);
        pnlDirecciones.add(new JLabel()); pnlDirecciones.add(btnSur); pnlDirecciones.add(new JLabel());
        
        pnlSur.add(pnlDirecciones, BorderLayout.WEST);
        
        // Campo de comandos
        JPanel pnlComando = new JPanel(new BorderLayout());
        pnlComando.setBackground(new Color(30, 30, 40));
        pnlComando.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        txtComando = new JTextField();
        txtComando.setBackground(Color.BLACK);
        txtComando.setForeground(Color.GREEN);
        txtComando.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtComando.setCaretColor(Color.GREEN);
        txtComando.addActionListener((ActionEvent e) -> {
            String input = txtComando.getText();
            txtComando.setText("");
            if (!input.trim().isEmpty()) {
                onMessage("> " + input);
                engine.procesarComando(input);
            }
        });
        
        pnlComando.add(new JLabel("Comando: "), BorderLayout.WEST);
        pnlComando.add(txtComando, BorderLayout.CENTER);
        
        pnlSur.add(pnlComando, BorderLayout.CENTER);
        
        add(pnlSur, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, String dir) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(50, 50, 70));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> {
            onMessage("> ir " + dir);
            engine.procesarComando("ir " + dir);
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
        if (win) {
            JOptionPane.showMessageDialog(this, "¡Felicidades! Has escapado de la estacion espacial.", "Mision Cumplida", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Te has quedado sin oxigeno. La estacion sera tu tumba...", "Game Over", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onRoomChange(Stanza room) {
        lblHabitacion.setText("Sector: " + room.getNombre());
    }

    @Override
    public void onInventoryChange(Contenitore<Item> inventory) {
        modelInventario.clear();
        for (Item item : inventory.getElementos()) {
            modelInventario.addElement(item.getNombre());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}
