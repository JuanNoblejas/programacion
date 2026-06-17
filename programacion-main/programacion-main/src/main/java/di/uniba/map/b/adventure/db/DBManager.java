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
            
            // Insertar datos de prueba si esta vacio
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM logs_terminal");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO logs_terminal (log_text) VALUES ('Bitacora del Capitan: Sistema de navegacion fallando...')");
                stmt.execute("INSERT INTO logs_terminal (log_text) VALUES ('Soporte vital critico en sector 4')");
            }
        } catch (SQLException e) {
            System.err.println("Error inicializando la base de datos: " + e.getMessage());
        }
    }

    public static String getRandomLog() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT log_text FROM logs_terminal ORDER BY RAND() LIMIT 1");
            if (rs.next()) {
                return rs.getString("log_text");
            }
        } catch (SQLException e) {
            return "Error accediendo a los logs de la base de datos.";
        }
        return "No hay logs disponibles.";
    }

    public static void guardarPuntuacion(String nombre, int score) {
        String sql = "INSERT INTO puntuacion (nombre, score) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error guardando puntuacion: " + e.getMessage());
        }
    }
}
