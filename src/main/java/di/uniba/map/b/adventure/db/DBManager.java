package di.uniba.map.b.adventure.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {
    private static final String URL = "jdbc:h2:./odisea_db";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initDB() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS logs_terminal (id INT AUTO_INCREMENT PRIMARY KEY, log_text VARCHAR(500))");
            stmt.execute("CREATE TABLE IF NOT EXISTS puntuacion (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(50), score INT)");
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM logs_terminal");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO logs_terminal (log_text) VALUES ('Captain''s Log - Day 12: All systems nominal. Routine maintenance completed.')");
                stmt.execute("INSERT INTO logs_terminal (log_text) VALUES ('Captain''s Log - Day 25: We detected an anomaly in the engineering sector. Will investigate tomorrow.')");
                stmt.execute("INSERT INTO logs_terminal (log_text) VALUES ('ALERT: Life support critical in sector 4. Immediate evacuation required.')");
                stmt.execute("INSERT INTO logs_terminal (log_text) VALUES ('Chief Engineer: The electrical panel in engineering is damaged. Without it, there is no power for the systems.')");
                stmt.execute("INSERT INTO logs_terminal (log_text) VALUES ('Captain''s Log - Day 47: The escape module locked down. I set the security code: 2847')");
                stmt.execute("INSERT INTO logs_terminal (log_text) VALUES ('Comms Officer: The main antenna is broken. Unable to contact the rescue fleet.')");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing the database: " + e.getMessage());
        }
    }

    public static String getRandomLog() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT log_text FROM logs_terminal ORDER BY RAND() LIMIT 1");
            if (rs.next()) {
                return rs.getString("log_text");
            }
        } catch (SQLException e) {
            return "Error accessing the database logs.";
        }
        return "No logs available.";
    }

    /**
     * Returns all logs from the database, simulating a Mainframe data dump.
     * Used when the player hacks the Control Room with the Access Card.
     */
    public static String getAllLogs() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT log_text FROM logs_terminal ORDER BY id");
            while (rs.next()) {
                sb.append("  > ").append(rs.getString("log_text")).append("\n");
            }
        } catch (SQLException e) {
            return "Error accessing the database logs.";
        }
        return sb.length() > 0 ? sb.toString() : "No logs available.";
    }

    public static void guardarPuntuacion(String nombre, int score) {
        String sql = "INSERT INTO puntuacion (nombre, score) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving score: " + e.getMessage());
        }
    }
}
