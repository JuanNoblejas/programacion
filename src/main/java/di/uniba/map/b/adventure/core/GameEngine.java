package di.uniba.map.b.adventure.core;

import di.uniba.map.b.adventure.db.DBManager;
import di.uniba.map.b.adventure.entities.Consumible;
import di.uniba.map.b.adventure.entities.Contenitore;
import di.uniba.map.b.adventure.entities.Direction;
import di.uniba.map.b.adventure.entities.Herramienta;
import di.uniba.map.b.adventure.entities.Item;
import di.uniba.map.b.adventure.entities.ObjetoJuego;
import di.uniba.map.b.adventure.entities.Stanza;
import di.uniba.map.b.adventure.io.SaveLoadManager;
import di.uniba.map.b.adventure.net.NetworkTerminal;
import di.uniba.map.b.adventure.parser.Parser;
import di.uniba.map.b.adventure.threads.OxygenTimer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GameEngine implements OxygenTimer.OxygenListener {
    // =======================================================================
    // TEMPORAL: PARA TESTING — Cambiar a false para reactivar el temporizador
    // =======================================================================
    private static final boolean TIMER_DESACTIVADO = false;
    // =======================================================================

    private GameState state;
    private Parser parser;
    private OxygenTimer timer;
    private EngineListener listener;
    private boolean juegoTerminado = false;
    private boolean vitrinaRota = false;
    private boolean puzzleLabResuelto = false;
    private boolean ingDoorUnlocked = false;

    public interface EngineListener {
        void onMessage(String msg);
        void onOxygenUpdate(int oxigeno);
        void onGameOver(boolean win);
        void onRoomChange(Stanza room);
        void onInventoryChange(Contenitore<ObjetoJuego> inventory);
    }

    public GameEngine(EngineListener listener) {
        this.listener = listener;
        this.parser = new Parser();
    }

    public void iniciarJuego() {
        DBManager.initDB();
        NetworkTerminal.startServer();
        crearMapa();
        iniciarTimer(100);
        listener.onRoomChange(state.habitacionActual);
        listener.onMessage("You wake up in " + state.habitacionActual.getNombre() + ". " + state.habitacionActual.getDescripcion());
        listener.onMessage("Life support is failing. You must reach the Escape Module.");
    }

    private void crearMapa() {
        Stanza criogenia = new Stanza("crio", "Cryogenics", "A cold room with empty capsules. A door leads south.");
        Stanza pasillo = new Stanza("pasillo", "Central Hallway", "A long dark hallway. Doors to the north, south, east and west.");
        Stanza control = new Stanza("control", "Control Room", "Flickering monitors. You can go west or east.");
        Stanza ingenieria = new Stanza("ing", "Engineering", "Loud engines. A glass display case holds a tool. Passages lead east and south.");
        Stanza comunicaciones = new Stanza("com", "Communications", "Broken radio equipment. One terminal seems to work. Exit to the north.");
        Stanza laboratorio = new Stanza("lab", "Laboratory", "Destroyed biological samples. There is an electronic panel locked with a security code. Exit to the west.");
        Stanza escape = new Stanza("escape", "Escape Module", "An emergency escape pod sits in the center. Its launch console is jammed — the bolts are rusted shut. You'd need a heavy wrench to pry it open. Exit to the north.");

        criogenia.setSalida(Direction.SOUTH, pasillo);
        pasillo.setSalida(Direction.NORTH, criogenia);
        pasillo.setSalida(Direction.EAST, control);
        pasillo.setSalida(Direction.WEST, ingenieria);
        pasillo.setSalida(Direction.SOUTH, comunicaciones);
        
        control.setSalida(Direction.WEST, pasillo);
        control.setSalida(Direction.EAST, laboratorio);
        laboratorio.setSalida(Direction.WEST, control);
        
        ingenieria.setSalida(Direction.EAST, pasillo);
        ingenieria.setSalida(Direction.SOUTH, escape);
        escape.setSalida(Direction.NORTH, ingenieria);
        comunicaciones.setSalida(Direction.NORTH, pasillo);

        // Objects — using concrete subclasses of ObjetoJuego
        criogenia.getObjetos().add(new Item("tarjeta", "Keycard", "A security access keycard."));
        ingenieria.getObjetos().add(new Herramienta("llave", "Wrench", "A heavy tool for repairing things. [Inside a glass display case]"));
        control.getObjetos().add(new Consumible("tanque", "Oxygen Tank", "Provides 60 extra seconds of air."));
        laboratorio.getObjetos().add(new Herramienta("martillo", "Hammer", "A heavy emergency hammer. Can break things."));

        List<Stanza> mapa = new ArrayList<>();
        mapa.add(criogenia); mapa.add(pasillo); mapa.add(control);
        mapa.add(ingenieria); mapa.add(comunicaciones); mapa.add(laboratorio);
        mapa.add(escape);

        state = new GameState(criogenia, new Contenitore<>(), 100, mapa);
    }

    private void iniciarTimer(int oxigeno) {
        if (timer != null) timer.detener();
        // TEMPORAL: Si TIMER_DESACTIVADO, no iniciamos el timer
        if (TIMER_DESACTIVADO) {
            listener.onOxygenUpdate(100);
            return;
        }
        timer = new OxygenTimer(oxigeno, this);
        timer.start();
    }

    /**
     * Pausa el temporizador de oxigeno.
     */
    public void pausarTimer() {
        if (timer != null && !TIMER_DESACTIVADO) {
            timer.pausar();
        }
    }

    /**
     * Reanuda el temporizador de oxigeno.
     */
    public void reanudarTimer() {
        if (timer != null && !TIMER_DESACTIVADO) {
            timer.reanudar();
        }
    }

    /**
     * Devuelve true si el juego esta pausado.
     */
    public boolean isPausado() {
        if (timer == null || TIMER_DESACTIVADO) return false;
        return timer.isPausado();
    }

    public void procesarComando(String input) {
        if (juegoTerminado) return;
        
        Parser.ResultadoParser res = parser.parse(input);
        
        switch (res.tipo) {
            case IR:
                mover(res.argumento);
                break;
            case MIRAR:
                mirar();
                break;
            case TOMAR:
                tomar(res.argumento);
                break;
            case USAR:
                usar(res.argumento);
                break;
            case INVENTARIO:
                mostrarInventario();
                break;
            case HACKEAR:
                hackear();
                break;
            case ROMPER:
                romper(res.argumento);
                break;
            case RESOLVER:
                resolver(res.argumento);
                break;
            case GUARDAR:
                guardar();
                break;
            case CARGAR:
                cargar();
                break;
            case AYUDA:
                listener.onMessage("Commands: go [n/s/e/w], look, take [obj], use [obj], break [obj], solve [answer], inv, hack, save, load");
                break;
            default:
                listener.onMessage("I don't understand that command.");
        }
    }

    private void mover(String dir) {
        Direction direction = Direction.fromString(dir);
        if (direction == null) {
            listener.onMessage("Invalid direction.");
            return;
        }

        // Block access to Engineering if the door puzzle is not solved
        if (state.habitacionActual.getId().equals("pasillo") && direction == Direction.WEST && !ingDoorUnlocked) {
            listener.onMessage("The door to Engineering is locked with an electronic keypad.");
            listener.onMessage("A screen displays: \"Look for the prime suspects\"");
            listener.onMessage("Type: solve [your answer]");
            return;
        }
        
        Stanza siguiente = state.habitacionActual.getSalida(direction);
        if (siguiente != null) {
            state.habitacionActual = siguiente;
            listener.onRoomChange(state.habitacionActual);
            listener.onMessage("You move " + direction.name().toLowerCase() + ". You arrived at: " + state.habitacionActual.getNombre());
        } else {
            listener.onMessage("You can't go in that direction.");
        }
    }

    private void mirar() {
        listener.onMessage(state.habitacionActual.getDescripcion());
        Set<ObjetoJuego> items = state.habitacionActual.getObjetos().getElementos();
        if (!items.isEmpty()) {
            listener.onMessage("You see the following objects here:");
            for (ObjetoJuego i : items) {
                listener.onMessage("- " + i.getNombre());
            }
        }
    }

    private void tomar(String nombreItem) {
        List<ObjetoJuego> filtrados = state.habitacionActual.getObjetos().filtrarPorNombre(nombreItem);
        if (!filtrados.isEmpty()) {
            ObjetoJuego item = filtrados.get(0);
            // Block taking the wrench if the display case is not broken
            if (item.getId().equals("llave") && !vitrinaRota) {
                listener.onMessage("The Wrench is inside a sealed glass display case. You need something to break it.");
                return;
            }
            // Block taking the hammer if the puzzle is not solved
            if (item.getId().equals("martillo") && !puzzleLabResuelto) {
                listener.onMessage("The Hammer is behind a locked electronic panel. You must solve the security code first.");
                listener.onMessage("Hint: 'The number of planets in the solar system, multiplied by the number of months with 31 days.'");
                listener.onMessage("Type: solve [your answer]");
                return;
            }
            state.habitacionActual.getObjetos().remove(item);
            state.inventario.add(item);
            listener.onMessage("You took: " + item.getNombre());
            listener.onInventoryChange(state.inventario);
        } else {
            listener.onMessage("I don't see that object here.");
        }
    }

    private void usar(String nombreItem) {
        List<ObjetoJuego> filtrados = state.inventario.filtrarPorNombre(nombreItem);
        if (filtrados.isEmpty()) {
            listener.onMessage("You don't have that object.");
            return;
        }
        ObjetoJuego item = filtrados.get(0);
        
        if (item.getId().equals("tanque")) {
            timer.addOxigeno(60);
            state.inventario.remove(item);
            listener.onMessage("You used the tank. +60 seconds of oxygen.");
            listener.onInventoryChange(state.inventario);
        } else if (item.getId().equals("llave") && state.habitacionActual.getId().equals("escape")) {
            listener.onMessage("You use the Wrench to repair the module's console.");
            listener.onMessage("SYSTEM ONLINE! You escaped the station. YOU WIN!");
            juegoTerminado = true;
            if (timer != null) timer.detener();
            DBManager.guardarPuntuacion("Player1", timer.getOxigenoRestante());
            listener.onGameOver(true);
        } else if (item.getId().equals("martillo") && state.habitacionActual.getId().equals("ing")) {
            // Using the hammer in Engineering breaks the display case
            if (vitrinaRota) {
                listener.onMessage("The display case is already broken. You can take the Wrench.");
            } else {
                vitrinaRota = true;
                listener.onMessage("CRASH! You use the Hammer to smash the glass display case.");
                listener.onMessage("Glass shards fall to the floor. The Wrench is now accessible.");
            }
        } else {
            listener.onMessage("You can't use that here or it has no effect.");
        }
    }

    private void mostrarInventario() {
        Set<ObjetoJuego> items = state.inventario.getElementos();
        if (items.isEmpty()) {
            listener.onMessage("Your inventory is empty.");
        } else {
            listener.onMessage("You are carrying:");
            for (ObjetoJuego i : items) {
                listener.onMessage("- " + i.getNombre());
            }
        }
    }

    private void hackear() {
        if (state.habitacionActual.getId().equals("comunicaciones")) {
            listener.onMessage("Hacking network terminal...");
            String msg = NetworkTerminal.getMessageFromServer();
            listener.onMessage(msg);
        } else if (state.habitacionActual.getId().equals("control")) {
            listener.onMessage("Accessing local database...");
            String log = DBManager.getRandomLog();
            listener.onMessage("LOG: " + log);
        } else {
            listener.onMessage("There are no useful terminals here.");
        }
    }

    private void romper(String objetivo) {
        if (state.habitacionActual.getId().equals("ing")) {
            if (vitrinaRota) {
                listener.onMessage("The display case is already broken. You can take the Wrench.");
                return;
            }
            // Check if the player has the hammer
            List<ObjetoJuego> martillos = state.inventario.filtrarPorNombre("hammer");
            if (martillos.isEmpty()) {
                martillos = state.inventario.filtrarPorNombre("martillo");
            }
            if (!martillos.isEmpty()) {
                vitrinaRota = true;
                listener.onMessage("CRASH! You use the Hammer to smash the glass display case.");
                listener.onMessage("Glass shards fall to the floor. The Wrench is now accessible.");
            } else {
                listener.onMessage("You try to hit the display case with your hands but it's too tough. You need something heavier.");
            }
        } else {
            listener.onMessage("There is nothing to break here.");
        }
    }

    private void resolver(String respuesta) {
        // Engineering door puzzle (from Central Hallway)
        if (state.habitacionActual.getId().equals("pasillo") && !ingDoorUnlocked) {
            if (respuesta.trim().equals("2357")) {
                ingDoorUnlocked = true;
                listener.onMessage("CORRECT! The door to Engineering slides open with a hiss.");
                listener.onMessage("You can now go west.");
            } else {
                listener.onMessage("Incorrect code. The keypad buzzes angrily.");
                listener.onMessage("Hint: \"Look for the prime suspects\"");
                listener.onMessage("Type: solve [your answer]");
            }
            return;
        }

        // Laboratory panel puzzle
        if (state.habitacionActual.getId().equals("lab")) {
            if (puzzleLabResuelto) {
                listener.onMessage("You already solved the code. The panel is open.");
                return;
            }
            // Correct answer: 8 planets * 7 months with 31 days = 56
            if (respuesta.trim().equals("56")) {
                puzzleLabResuelto = true;
                listener.onMessage("CORRECT! The panel opens with a mechanical click.");
                listener.onMessage("Inside you find an emergency Hammer. You can now take it.");
            } else {
                listener.onMessage("Incorrect code. The panel emits an error beep.");
                listener.onMessage("Hint: 'The number of planets in the solar system, multiplied by the number of months with 31 days.'");
                listener.onMessage("Type: solve [your answer]");
            }
            return;
        }

        listener.onMessage("There is no puzzle to solve here.");
    }

    private void guardar() {
        if (timer != null) state.oxigenoRestante = timer.getOxigenoRestante();
        state.vitrinaRota = this.vitrinaRota;
        state.puzzleLabResuelto = this.puzzleLabResuelto;
        state.ingDoorUnlocked = this.ingDoorUnlocked;
        if (SaveLoadManager.guardar(state)) {
            listener.onMessage("Game saved.");
        } else {
            listener.onMessage("Error saving game.");
        }
    }

    private void cargar() {
        GameState cargado = SaveLoadManager.cargar();
        if (cargado != null) {
            this.state = cargado;
            this.vitrinaRota = state.vitrinaRota;
            this.puzzleLabResuelto = state.puzzleLabResuelto;
            this.ingDoorUnlocked = state.ingDoorUnlocked;
            iniciarTimer(state.oxigenoRestante);
            listener.onRoomChange(state.habitacionActual);
            listener.onInventoryChange(state.inventario);
            listener.onMessage("Game loaded successfully.");
        } else {
            listener.onMessage("Error loading game.");
        }
    }

    @Override
    public void onOxygenUpdate(int oxigeno) {
        listener.onOxygenUpdate(oxigeno);
    }

    @Override
    public void onGameOver() {
        if (!juegoTerminado) {
            juegoTerminado = true;
            listener.onMessage("You ran out of oxygen... Game over.");
            listener.onGameOver(false);
        }
    }
}
