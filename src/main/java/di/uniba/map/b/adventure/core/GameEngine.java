package di.uniba.map.b.adventure.core;

import di.uniba.map.b.adventure.db.DBManager;
import di.uniba.map.b.adventure.entities.Contenitore;
import di.uniba.map.b.adventure.entities.Item;
import di.uniba.map.b.adventure.entities.Stanza;
import di.uniba.map.b.adventure.io.SaveLoadManager;
import di.uniba.map.b.adventure.net.NetworkTerminal;
import di.uniba.map.b.adventure.parser.Parser;
import di.uniba.map.b.adventure.threads.OxygenTimer;
import java.util.ArrayList;
import java.util.List;

public class GameEngine implements OxygenTimer.OxygenListener {
    // =======================================================================
    // TEMPORARY: FOR TESTING — Set to false to reactivate the timer
    // =======================================================================
    private static final boolean TIMER_DISABLED = false;
    // =======================================================================

    private static final int INITIAL_OXYGEN = 300;

    private GameState state;
    private Parser parser;
    private OxygenTimer timer;
    private EngineListener listener;
    private boolean gameOver = false;

    public interface EngineListener {
        void onMessage(String msg);
        void onOxygenUpdate(int oxygen);
        void onGameOver(boolean win);
        void onRoomChange(Stanza room);
        void onInventoryChange(Contenitore<Item> inventory);
    }

    public GameEngine(EngineListener listener) {
        this.listener = listener;
        this.parser = new Parser();
    }

    public void iniciarJuego() {
        DBManager.initDB();
        NetworkTerminal.startServer();
        createMap();
        startTimer(INITIAL_OXYGEN);
        listener.onRoomChange(state.habitacionActual);
        listener.onMessage("You wake up in " + state.habitacionActual.getNombre() + ". " + state.habitacionActual.getDescripcion());
        listener.onMessage("Life support is failing. You must reach the Escape Module.");
        listener.onMessage("But its hatch is sealed with a 3-factor security protocol.");
        listener.onMessage("Explore the station, repair the systems and gather the clues you need.");
    }

    private void createMap() {
        // === ROOMS ===
        Stanza cryogenics = new Stanza("crio", "Cryogenics",
            "A freezing room with empty cryogenic pods. Frost crystals cover the walls. "
            + "On the wall panel you spot a forgotten access card. "
            + "Next to the ducts, a spare antenna module. A door leads south.");

        Stanza hallway = new Stanza("pasillo", "Central Hallway",
            "A long, dark corridor. Emergency lights flash red. "
            + "Doors to the north, south, east and west.");

        Stanza control = new Stanza("control", "Control Room",
            "Flickering monitors surround a large central Mainframe. "
            + "The main screen is off — there seems to be no power. "
            + "You can go west or east.");

        Stanza engineering = new Stanza("ing", "Engineering",
            "Noisy engines and rusty pipes. An electrical panel on the wall is damaged, "
            + "with loose cables sparking. Without fixing it, the station will have no power. "
            + "A wrench rests on the workbench. Passages east and south.");

        Stanza comms = new Stanza("com", "Communications",
            "Radio equipment and frequency displays fill the room. "
            + "An intergalactic communications terminal sits in the center, but its antenna is wrecked. "
            + "Exit to the north.");

        Stanza lab = new Stanza("lab", "Laboratory",
            "Destroyed biological samples and broken test tubes litter the floor. "
            + "Among the debris, a leather-bound journal catches your eye. "
            + "Exit to the west.");

        Stanza escape = new Stanza("escape", "Escape Module",
            "The evacuation pod is here. Emergency lights blink green. "
            + "The ignition panel needs a final repair. Exit to the north.");

        // === CONNECTIONS ===
        cryogenics.setSalida("south", hallway);
        hallway.setSalida("north", cryogenics);
        hallway.setSalida("east", control);
        hallway.setSalida("west", engineering);
        hallway.setSalida("south", comms);

        control.setSalida("west", hallway);
        control.setSalida("east", lab);
        lab.setSalida("west", control);

        engineering.setSalida("east", hallway);
        engineering.setSalida("south", escape);
        escape.setSalida("north", engineering);
        comms.setSalida("north", hallway);

        // === ITEMS ===
        // Cryogenics: Access Card + Antenna Module
        cryogenics.getObjetos().add(new Item("card", "Access Card",
            "A security access card. It has the captain's emblem engraved on it."));
        cryogenics.getObjetos().add(new Item("antenna", "Antenna Module",
            "A spare part for communication systems. It looks compatible with a standard antenna."));

        // Engineering: Wrench
        engineering.getObjetos().add(new Item("wrench", "Wrench",
            "A heavy tool for repairing panels and mechanisms."));

        // Control Room: Oxygen Tank
        control.getObjetos().add(new Item("tank", "Oxygen Tank",
            "An emergency oxygen tank. Provides 60 extra seconds of air."));

        // Laboratory: Captain's Journal
        lab.getObjetos().add(new Item("journal", "Captain's Journal",
            "A leather-bound journal with the name 'Captain Vasquez' engraved on the cover."));

        // === MAP ===
        List<Stanza> map = new ArrayList<>();
        map.add(cryogenics); map.add(hallway); map.add(control);
        map.add(engineering); map.add(comms); map.add(lab);
        map.add(escape);

        state = new GameState(cryogenics, new Contenitore<>(), INITIAL_OXYGEN, map);
    }

    private void startTimer(int oxygen) {
        if (timer != null) timer.detener();
        // TEMPORARY: If TIMER_DISABLED, skip the timer
        if (TIMER_DISABLED) {
            listener.onOxygenUpdate(INITIAL_OXYGEN);
            return;
        }
        timer = new OxygenTimer(oxygen, this);
        timer.start();
    }

    /**
     * Pauses the oxygen timer.
     */
    public void pausarTimer() {
        if (timer != null && !TIMER_DISABLED) {
            timer.pausar();
        }
    }

    /**
     * Resumes the oxygen timer.
     */
    public void reanudarTimer() {
        if (timer != null && !TIMER_DISABLED) {
            timer.reanudar();
        }
    }

    /**
     * Returns true if the game is paused.
     */
    public boolean isPausado() {
        if (timer == null || TIMER_DISABLED) return false;
        return timer.isPausado();
    }

    public void procesarComando(String input) {
        if (gameOver) return;

        Parser.ResultadoParser res = parser.parse(input);

        switch (res.tipo) {
            case IR:
                move(res.argumento);
                break;
            case MIRAR:
                look();
                break;
            case TOMAR:
                take(res.argumento);
                break;
            case USAR:
                use(res.argumento);
                break;
            case INVENTARIO:
                showInventory();
                break;
            case HACKEAR:
                hack();
                break;
            case GUARDAR:
                save();
                break;
            case CARGAR:
                load();
                break;
            case AYUDA:
                listener.onMessage("Commands: go [n/s/e/w], look, take [obj], use [obj], inv, hack, save, load");
                break;
            default:
                listener.onMessage("I don't understand that command.");
        }
    }

    // =========================================================================
    // MOVEMENT — Blocks access to the Escape Module without the 3 factors
    // =========================================================================
    private void move(String dir) {
        Stanza next = state.habitacionActual.getSalida(dir);
        if (next == null) {
            listener.onMessage("You can't go that way.");
            return;
        }

        // Block entry to the Escape Module without the 3 factors
        if (next.getId().equals("escape")) {
            if (!state.energiaRestaurada || !state.secuenciaObtenida || !hasItem("chip")) {
                listener.onMessage("The Escape Module hatch is hermetically sealed.");
                listener.onMessage("A panel reads: 'SECURITY PROTOCOL ACTIVE — 3 authentication factors required.'");
                if (!state.energiaRestaurada) {
                    listener.onMessage("  [X] Factor 1: Station power not restored.");
                } else {
                    listener.onMessage("  [OK] Factor 1: Power restored.");
                }
                if (!hasItem("chip")) {
                    listener.onMessage("  [X] Factor 2: Memory chip not inserted.");
                } else {
                    listener.onMessage("  [OK] Factor 2: Memory chip available.");
                }
                if (!state.secuenciaObtenida) {
                    listener.onMessage("  [X] Factor 3: Unlock sequence not received.");
                } else {
                    listener.onMessage("  [OK] Factor 3: Rescue sequence confirmed.");
                }
                listener.onMessage("You cannot pass.");
                return;
            }
            // All 3 factors complete — open the hatch
            listener.onMessage("The hatch recognizes your credentials...");
            listener.onMessage("All 3 security factors validated:");
            listener.onMessage("  [OK] Power restored.");
            listener.onMessage("  [OK] Memory chip inserted (Code: 2847).");
            listener.onMessage("  [OK] Rescue sequence confirmed (OMEGA-2847-ESCAPE).");
            listener.onMessage("The hatches open with a thunderous roar!");
        }

        state.habitacionActual = next;
        listener.onRoomChange(state.habitacionActual);
        listener.onMessage("You move " + dir + ". You arrived at: " + state.habitacionActual.getNombre());
        listener.onMessage(state.habitacionActual.getDescripcion());
    }

    // =========================================================================
    // LOOK
    // =========================================================================
    private void look() {
        listener.onMessage(state.habitacionActual.getDescripcion());
        List<Item> items = state.habitacionActual.getObjetos().getElementos();
        if (!items.isEmpty()) {
            listener.onMessage("You see the following items here:");
            for (Item i : items) {
                listener.onMessage("- " + i.getNombre() + ": " + i.getDescripcion());
            }
        }
    }

    // =========================================================================
    // TAKE
    // =========================================================================
    private void take(String itemName) {
        List<Item> filtered = state.habitacionActual.getObjetos().filtrarPorNombre(itemName);
        if (!filtered.isEmpty()) {
            Item item = filtered.get(0);
            state.habitacionActual.getObjetos().remove(item);
            state.inventario.add(item);
            listener.onMessage("You took: " + item.getNombre());
            listener.onInventoryChange(state.inventario);
        } else {
            listener.onMessage("I don't see that item here.");
        }
    }

    // =========================================================================
    // USE — Expanded logic with per-room puzzles
    // =========================================================================
    private void use(String itemName) {
        List<Item> filtered = state.inventario.filtrarPorNombre(itemName);
        if (filtered.isEmpty()) {
            listener.onMessage("You don't have that item.");
            return;
        }
        Item item = filtered.get(0);
        String roomId = state.habitacionActual.getId();

        // --- OXYGEN TANK (any room) ---
        if (item.getId().equals("tank")) {
            if (timer != null) timer.addOxigeno(60);
            state.inventario.remove(item);
            listener.onMessage("You used the oxygen tank. +60 seconds of air.");
            listener.onInventoryChange(state.inventario);
            return;
        }

        // --- WRENCH in ENGINEERING: repair electrical panel ---
        if (item.getId().equals("wrench") && roomId.equals("ing")) {
            if (state.energiaRestaurada) {
                listener.onMessage("You already repaired the electrical panel. Power is running.");
                return;
            }
            state.energiaRestaurada = true;
            listener.onMessage("You use the Wrench on the damaged electrical panel...");
            listener.onMessage("Sparks! The circuits reconnect one by one...");
            listener.onMessage("Power has been restored to the station's systems!");
            listener.onMessage("You can now hack the terminals in Control and Communications.");
            // Update room description
            state.habitacionActual.setDescripcion(
                "Noisy engines and rusty pipes. The electrical panel is now working properly, "
                + "with green lights blinking. Passages east and south.");
            return;
        }

        // --- WRENCH in ESCAPE MODULE: final victory ---
        if (item.getId().equals("wrench") && roomId.equals("escape")) {
            listener.onMessage("You use the Wrench to couple the ignition panel...");
            listener.onMessage("The engines roar to life!");
            listener.onMessage("The evacuation pod detaches from the station...");
            listener.onMessage("Through the porthole you watch the station drift away into the darkness of space.");
            listener.onMessage("YOU ESCAPED! VICTORY!");
            gameOver = true;
            if (timer != null) {
                timer.detener();
                DBManager.guardarPuntuacion("Player1", timer.getOxigenoRestante());
            }
            listener.onGameOver(true);
            return;
        }

        // --- CAPTAIN'S JOURNAL (any room) ---
        if (item.getId().equals("journal")) {
            listener.onMessage("You open the Captain's Journal. On the last page, in shaky handwriting, you read:");
            listener.onMessage("  'If anyone is still alive... the distress frequency to contact");
            listener.onMessage("   the rescue fleet is OMEGA-7. Transmit it from Communications.");
            listener.onMessage("   They have the final unlock sequence.'");
            state.frecuenciaDescubierta = true;
            listener.onMessage("[Clue discovered: frequency OMEGA-7]");
            return;
        }

        // --- ANTENNA MODULE in COMMUNICATIONS: repair antenna ---
        if (item.getId().equals("antenna") && roomId.equals("com")) {
            if (state.antenaReparada) {
                listener.onMessage("The antenna is already repaired.");
                return;
            }
            state.antenaReparada = true;
            state.inventario.remove(item);
            listener.onMessage("You install the Antenna Module on the damaged receiver...");
            listener.onMessage("The signal stabilizes! The communications terminal is now operational.");
            listener.onInventoryChange(state.inventario);
            // Update room description
            state.habitacionActual.setDescripcion(
                "Radio equipment and frequency displays fill the room. "
                + "The intergalactic communications terminal is operational, antenna repaired. "
                + "Exit to the north.");
            return;
        }

        // --- ACCESS CARD in CONTROL: contextual hint ---
        if (item.getId().equals("card") && roomId.equals("control")) {
            listener.onMessage("You hold the Access Card near the Mainframe reader. The light turns from red to green.");
            listener.onMessage("Use the 'hack' command to access the system.");
            return;
        }

        // --- Default ---
        listener.onMessage("You can't use '" + item.getNombre() + "' here. It has no effect.");
    }

    // =========================================================================
    // HACK — Expanded with per-room requirements
    // =========================================================================
    private void hack() {
        String roomId = state.habitacionActual.getId();

        // === CONTROL ROOM: Mainframe ===
        if (roomId.equals("control")) {
            if (!state.energiaRestaurada) {
                listener.onMessage("The Mainframe screen is dark. There is no power.");
                listener.onMessage("You must restore the station's power first.");
                return;
            }
            if (!hasItem("card")) {
                listener.onMessage("The Mainframe requires an authorization card to access.");
                listener.onMessage("You need to find an Access Card.");
                return;
            }
            // Already has chip — don't repeat
            if (hasItem("chip")) {
                listener.onMessage("You have already extracted all information from the Mainframe.");
                return;
            }
            listener.onMessage("You insert the Access Card into the Mainframe reader...");
            listener.onMessage("Accessing the central system database...");
            listener.onMessage("--- CREW LOGS ---");
            String logs = DBManager.getAllLogs();
            listener.onMessage(logs);
            listener.onMessage("--- END OF LOGS ---");
            listener.onMessage("You extracted a Memory Chip with the security protocol code.");
            listener.onMessage("[Item obtained: Memory Chip (code 2847)]");

            // Add chip to inventory
            Item chip = new Item("chip", "Memory Chip",
                "A memory chip extracted from the Mainframe. Contains the security code: 2847.");
            state.inventario.add(chip);
            listener.onInventoryChange(state.inventario);

            // Update room description
            state.habitacionActual.setDescripcion(
                "Flickering monitors surround the central Mainframe. "
                + "The screen reads 'DATA EXTRACTED — SESSION CLOSED'. "
                + "You can go west or east.");
            return;
        }

        // === COMMUNICATIONS: Intergalactic terminal ===
        if (roomId.equals("com")) {
            if (!state.energiaRestaurada) {
                listener.onMessage("The communications terminal has no power.");
                listener.onMessage("You must restore the station's power first.");
                return;
            }
            if (!state.antenaReparada) {
                listener.onMessage("The communications antenna is wrecked. You can't transmit anything.");
                listener.onMessage("You need to find a spare part for the antenna.");
                return;
            }
            if (!state.frecuenciaDescubierta) {
                listener.onMessage("The terminal asks for a transmission frequency...");
                listener.onMessage("You don't know which one to use. Maybe there's a clue somewhere on the station.");
                return;
            }
            if (state.secuenciaObtenida) {
                listener.onMessage("You already contacted the rescue fleet. The sequence is OMEGA-2847-ESCAPE.");
                return;
            }
            listener.onMessage("Connecting intergalactic terminal on frequency OMEGA-7...");
            listener.onMessage("Establishing socket link with the emergency network...");
            String msg = NetworkTerminal.getMessageFromServer();
            listener.onMessage(msg);
            state.secuenciaObtenida = true;
            listener.onMessage("[Unlock sequence obtained!]");

            // Update room description
            state.habitacionActual.setDescripcion(
                "Radio equipment and frequency displays fill the room. "
                + "The terminal reads: 'CONNECTION ESTABLISHED — OMEGA-2847-ESCAPE'. "
                + "Exit to the north.");
            return;
        }

        // === Other rooms ===
        listener.onMessage("There are no useful terminals here.");
    }

    // =========================================================================
    // INVENTORY
    // =========================================================================
    private void showInventory() {
        List<Item> items = state.inventario.getElementos();
        if (items.isEmpty()) {
            listener.onMessage("Your inventory is empty.");
        } else {
            listener.onMessage("You are carrying:");
            for (Item i : items) {
                listener.onMessage("- " + i.getNombre() + ": " + i.getDescripcion());
            }
        }
        // Show puzzle status
        listener.onMessage("--- Mission Status ---");
        listener.onMessage("  Power restored: " + (state.energiaRestaurada ? "YES" : "NO"));
        listener.onMessage("  Frequency discovered: " + (state.frecuenciaDescubierta ? "YES" : "NO"));
        listener.onMessage("  Antenna repaired: " + (state.antenaReparada ? "YES" : "NO"));
        listener.onMessage("  Sequence obtained: " + (state.secuenciaObtenida ? "YES" : "NO"));
    }

    // =========================================================================
    // SAVE / LOAD
    // =========================================================================
    private void save() {
        if (timer != null) state.oxigenoRestante = timer.getOxigenoRestante();
        if (SaveLoadManager.guardar(state)) {
            listener.onMessage("Game saved.");
        } else {
            listener.onMessage("Error saving the game.");
        }
    }

    private void load() {
        GameState loaded = SaveLoadManager.cargar();
        if (loaded != null) {
            this.state = loaded;
            startTimer(state.oxigenoRestante);
            listener.onRoomChange(state.habitacionActual);
            listener.onInventoryChange(state.inventario);
            listener.onMessage("Game loaded successfully.");
        } else {
            listener.onMessage("Error loading the game.");
        }
    }

    // =========================================================================
    // UTILITIES
    // =========================================================================

    /**
     * Checks whether the player has an item with the given ID in their inventory.
     */
    private boolean hasItem(String itemId) {
        return state.inventario.getElementos().stream()
            .anyMatch(item -> item.getId().equals(itemId));
    }

    // =========================================================================
    // OXYGEN TIMER CALLBACKS
    // =========================================================================
    @Override
    public void onOxygenUpdate(int oxygen) {
        listener.onOxygenUpdate(oxygen);
    }

    @Override
    public void onGameOver() {
        if (!gameOver) {
            gameOver = true;
            listener.onMessage("You ran out of oxygen... Game over.");
            listener.onGameOver(false);
        }
    }
}
