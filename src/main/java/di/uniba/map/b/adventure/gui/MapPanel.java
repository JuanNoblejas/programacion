package di.uniba.map.b.adventure.gui;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Panel que dibuja un mapa visual de la estacion espacial.
 * Las habitaciones se van desbloqueando (haciendose visibles)
 * conforme el jugador las visita.
 */
public class MapPanel extends JPanel {

    // Posiciones fijas de cada habitacion en la cuadricula del mapa (col, fila)
    private static final Map<String, int[]> POSICIONES = new LinkedHashMap<>();
    static {
        //                        col, fila
        POSICIONES.put("crio",    new int[]{2, 0});  // Criogenia           (arriba centro)
        POSICIONES.put("pasillo", new int[]{2, 1});  // Pasillo Central     (centro)
        POSICIONES.put("ing",     new int[]{1, 1});  // Ingenieria          (centro izquierda)
        POSICIONES.put("control", new int[]{3, 1});  // Sala de Control     (centro derecha)
        POSICIONES.put("com",     new int[]{2, 2});  // Comunicaciones      (abajo centro)
        POSICIONES.put("lab",     new int[]{4, 1});  // Laboratorio         (extremo derecha)
        POSICIONES.put("escape",  new int[]{1, 2});  // Modulo de Escape    (abajo izquierda)
    }

    // Nombres legibles para mostrar dentro de cada celda
    private static final Map<String, String> NOMBRES = new LinkedHashMap<>();
    static {
        NOMBRES.put("crio",    "Criogenia");
        NOMBRES.put("pasillo", "Pasillo");
        NOMBRES.put("ing",     "Ingenieria");
        NOMBRES.put("control", "Control");
        NOMBRES.put("com",     "Comunic.");
        NOMBRES.put("lab",     "Laborat.");
        NOMBRES.put("escape",  "ESCAPE");
    }

    // Conexiones entre habitaciones (pares de ids) para dibujar lineas
    private static final String[][] CONEXIONES = {
        {"crio", "pasillo"},
        {"pasillo", "ing"},
        {"pasillo", "control"},
        {"pasillo", "com"},
        {"control", "lab"},
        {"ing", "escape"}
    };

    private final Set<String> visitadas = new HashSet<>();
    private String habitacionActualId = "";

    // Colores tematicos sci-fi
    private static final Color COLOR_FONDO       = new Color(15, 15, 20);
    private static final Color COLOR_NO_VISITADA  = new Color(40, 40, 55);
    private static final Color COLOR_VISITADA     = new Color(30, 80, 120);
    private static final Color COLOR_ACTUAL       = new Color(0, 200, 100);
    private static final Color COLOR_ESCAPE       = new Color(200, 160, 0);
    private static final Color COLOR_LINEA        = new Color(60, 60, 80);
    private static final Color COLOR_LINEA_ACTIVA = new Color(0, 160, 220);
    private static final Color COLOR_TEXTO        = Color.WHITE;
    private static final Color COLOR_TEXTO_OCULTO = new Color(80, 80, 100);

    public MapPanel() {
        setBackground(COLOR_FONDO);
        setPreferredSize(new Dimension(260, 200));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 150), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    /**
     * Actualiza la habitacion actual y la marca como visitada.
     */
    public void actualizarHabitacion(String idHabitacion) {
        this.habitacionActualId = idHabitacion;
        this.visitadas.add(idHabitacion);
        repaint();
    }

    /**
     * Restaura el conjunto de habitaciones visitadas (para cargar partida).
     */
    public void setVisitadas(Set<String> ids) {
        visitadas.clear();
        visitadas.addAll(ids);
        repaint();
    }

    public Set<String> getVisitadas() {
        return new HashSet<>(visitadas);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cellW = 48;
        int cellH = 38;
        int gapX = 6;
        int gapY = 10;

        // Calcular offset para centrar el mapa en el panel
        int totalCols = 5;
        int totalRows = 3;
        int totalW = totalCols * (cellW + gapX) - gapX;
        int totalH = totalRows * (cellH + gapY) - gapY;
        int offsetX = (getWidth() - totalW) / 2;
        int offsetY = (getHeight() - totalH) / 2 + 8;

        // Titulo del mapa
        g2.setColor(new Color(0, 180, 220));
        g2.setFont(new Font("Consolas", Font.BOLD, 11));
        FontMetrics fmTitle = g2.getFontMetrics();
        String titulo = "[ MAPA DE LA ESTACION ]";
        g2.drawString(titulo, (getWidth() - fmTitle.stringWidth(titulo)) / 2, offsetY - 6);

        // 1. Dibujar conexiones (lineas)
        for (String[] con : CONEXIONES) {
            int[] posA = POSICIONES.get(con[0]);
            int[] posB = POSICIONES.get(con[1]);
            if (posA == null || posB == null) continue;

            boolean ambasVisitadas = visitadas.contains(con[0]) && visitadas.contains(con[1]);
            g2.setColor(ambasVisitadas ? COLOR_LINEA_ACTIVA : COLOR_LINEA);
            g2.setStroke(ambasVisitadas ? new BasicStroke(2f) : new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));

            int x1 = offsetX + posA[0] * (cellW + gapX) + cellW / 2;
            int y1 = offsetY + posA[1] * (cellH + gapY) + cellH / 2;
            int x2 = offsetX + posB[0] * (cellW + gapX) + cellW / 2;
            int y2 = offsetY + posB[1] * (cellH + gapY) + cellH / 2;
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(new BasicStroke(1f));

        // 2. Dibujar habitaciones
        g2.setFont(new Font("Consolas", Font.PLAIN, 9));
        FontMetrics fm = g2.getFontMetrics();

        for (Map.Entry<String, int[]> entry : POSICIONES.entrySet()) {
            String id = entry.getKey();
            int[] pos = entry.getValue();
            int x = offsetX + pos[0] * (cellW + gapX);
            int y = offsetY + pos[1] * (cellH + gapY);

            boolean esActual = id.equals(habitacionActualId);
            boolean esVisitada = visitadas.contains(id);
            boolean esEscape = id.equals("escape");

            // Color de fondo de la celda
            Color colorCelda;
            if (esActual) {
                colorCelda = COLOR_ACTUAL;
            } else if (esEscape && esVisitada) {
                colorCelda = COLOR_ESCAPE;
            } else if (esVisitada) {
                colorCelda = COLOR_VISITADA;
            } else {
                colorCelda = COLOR_NO_VISITADA;
            }

            // Dibujar celda redondeada
            g2.setColor(colorCelda);
            g2.fillRoundRect(x, y, cellW, cellH, 8, 8);

            // Borde
            if (esActual) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x, y, cellW, cellH, 8, 8);
                g2.setStroke(new BasicStroke(1f));
            } else {
                g2.setColor(colorCelda.brighter());
                g2.drawRoundRect(x, y, cellW, cellH, 8, 8);
            }

            // Nombre
            String nombre = esVisitada ? NOMBRES.getOrDefault(id, "?") : "???";
            g2.setColor(esVisitada ? COLOR_TEXTO : COLOR_TEXTO_OCULTO);
            int txtW = fm.stringWidth(nombre);
            g2.drawString(nombre, x + (cellW - txtW) / 2, y + cellH / 2 + fm.getAscent() / 2 - 1);
        }
    }
}
