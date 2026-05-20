package di.uniba.map.b.adventure.io;

import di.uniba.map.b.adventure.core.GameState;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SaveLoadManager {
    private static final String SAVE_FILE = "savegame.dat";

    public static boolean guardar(GameState state) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(state);
            return true;
        } catch (IOException e) {
            System.err.println("Error guardando la partida: " + e.getMessage());
            return false;
        }
    }

    public static GameState cargar() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            return (GameState) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error cargando la partida: " + e.getMessage());
            return null;
        }
    }
}
