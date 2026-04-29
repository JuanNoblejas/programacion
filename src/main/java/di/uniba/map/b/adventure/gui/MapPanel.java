package di.uniba.map.b.adventure.gui;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Panel that draws a visual map of the space station.
 * Rooms are revealed as the player visits them.
 */
public class MapPanel extends JPanel {

    // Fixed positions of each room on the map grid (col, row)
    private static final Map<String, int[]> POSITIONS = new LinkedHashMap<>();
    static {
        //                        col, row
        POSITIONS.put("crio",    new int[]{2, 0});  // Cryogenics        (top center)
        POSITIONS.put("pasillo", new int[]{2, 1});  // Central Hallway   (center)
        POSITIONS.put("ing",     new int[]{1, 1});  // Engineering       (center left)
        POSITIONS.put("control", new int[]{3, 1});  // Control Room      (center right)
        POSITIONS.put("com",     new int[]{2, 2});  // Communications    (bottom center)
        POSITIONS.put("lab",     new int[]{4, 1});  // Laboratory        (far right)
        POSITIONS.put("escape",  new int[]{1, 2});  // Escape Module     (bottom left)
    }

    // Readable names to display inside each cell
    private static final Map<String, String> NAMES = new LinkedHashMap<>();
    static {
        NAMES.put("crio",    "Cryogenics");
        NAMES.put("pasillo", "Hallway");
        NAMES.put("ing",     "Engineer.");
        NAMES.put("control", "Control");
        NAMES.put("com",     "Comms");
        NAMES.put("lab",     "Lab");
        NAMES.put("escape",  "ESCAPE");
    }

    // Connections between rooms (id pairs) to draw lines
    private static final String[][] CONNECTIONS = {
        {"crio", "pasillo"},
        {"pasillo", "ing"},
        {"pasillo", "control"},
        {"pasillo", "com"},
        {"control", "lab"},
        {"ing", "escape"}
    };

    private final Set<String> visited = new HashSet<>();
    private String currentRoomId = "";

    // Sci-fi themed colors
    private static final Color COLOR_BG          = new Color(15, 15, 20);
    private static final Color COLOR_UNVISITED   = new Color(40, 40, 55);
    private static final Color COLOR_VISITED      = new Color(30, 80, 120);
    private static final Color COLOR_CURRENT      = new Color(0, 200, 100);
    private static final Color COLOR_ESCAPE       = new Color(200, 160, 0);
    private static final Color COLOR_LINE         = new Color(60, 60, 80);
    private static final Color COLOR_LINE_ACTIVE  = new Color(0, 160, 220);
    private static final Color COLOR_TEXT         = Color.WHITE;
    private static final Color COLOR_TEXT_HIDDEN  = new Color(80, 80, 100);

    public MapPanel() {
        setBackground(COLOR_BG);
        setPreferredSize(new Dimension(260, 200));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 100, 150), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    /**
     * Updates the current room and marks it as visited.
     */
    public void actualizarHabitacion(String roomId) {
        this.currentRoomId = roomId;
        this.visited.add(roomId);
        repaint();
    }

    /**
     * Restores the set of visited rooms (for loading a saved game).
     */
    public void setVisitadas(Set<String> ids) {
        visited.clear();
        visited.addAll(ids);
        repaint();
    }

    public Set<String> getVisitadas() {
        return new HashSet<>(visited);
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

        // Calculate offset to center the map in the panel
        int totalCols = 5;
        int totalRows = 3;
        int totalW = totalCols * (cellW + gapX) - gapX;
        int totalH = totalRows * (cellH + gapY) - gapY;
        int offsetX = (getWidth() - totalW) / 2;
        int offsetY = (getHeight() - totalH) / 2 + 8;

        // Map title
        g2.setColor(new Color(0, 180, 220));
        g2.setFont(new Font("Consolas", Font.BOLD, 11));
        FontMetrics fmTitle = g2.getFontMetrics();
        String title = "[ STATION MAP ]";
        g2.drawString(title, (getWidth() - fmTitle.stringWidth(title)) / 2, offsetY - 6);

        // 1. Draw connections (lines)
        for (String[] con : CONNECTIONS) {
            int[] posA = POSITIONS.get(con[0]);
            int[] posB = POSITIONS.get(con[1]);
            if (posA == null || posB == null) continue;

            boolean bothVisited = visited.contains(con[0]) && visited.contains(con[1]);
            g2.setColor(bothVisited ? COLOR_LINE_ACTIVE : COLOR_LINE);
            g2.setStroke(bothVisited ? new BasicStroke(2f) : new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0));

            int x1 = offsetX + posA[0] * (cellW + gapX) + cellW / 2;
            int y1 = offsetY + posA[1] * (cellH + gapY) + cellH / 2;
            int x2 = offsetX + posB[0] * (cellW + gapX) + cellW / 2;
            int y2 = offsetY + posB[1] * (cellH + gapY) + cellH / 2;
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(new BasicStroke(1f));

        // 2. Draw rooms
        g2.setFont(new Font("Consolas", Font.PLAIN, 9));
        FontMetrics fm = g2.getFontMetrics();

        for (Map.Entry<String, int[]> entry : POSITIONS.entrySet()) {
            String id = entry.getKey();
            int[] pos = entry.getValue();
            int x = offsetX + pos[0] * (cellW + gapX);
            int y = offsetY + pos[1] * (cellH + gapY);

            boolean isCurrent = id.equals(currentRoomId);
            boolean isVisited = visited.contains(id);
            boolean isEscape = id.equals("escape");

            // Cell background color
            Color cellColor;
            if (isCurrent) {
                cellColor = COLOR_CURRENT;
            } else if (isEscape && isVisited) {
                cellColor = COLOR_ESCAPE;
            } else if (isVisited) {
                cellColor = COLOR_VISITED;
            } else {
                cellColor = COLOR_UNVISITED;
            }

            // Draw rounded cell
            g2.setColor(cellColor);
            g2.fillRoundRect(x, y, cellW, cellH, 8, 8);

            // Border
            if (isCurrent) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(x, y, cellW, cellH, 8, 8);
                g2.setStroke(new BasicStroke(1f));
            } else {
                g2.setColor(cellColor.brighter());
                g2.drawRoundRect(x, y, cellW, cellH, 8, 8);
            }

            // Name
            String name = isVisited ? NAMES.getOrDefault(id, "?") : "???";
            g2.setColor(isVisited ? COLOR_TEXT : COLOR_TEXT_HIDDEN);
            int txtW = fm.stringWidth(name);
            g2.drawString(name, x + (cellW - txtW) / 2, y + cellH / 2 + fm.getAscent() / 2 - 1);
        }
    }
}
