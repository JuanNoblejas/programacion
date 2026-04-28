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
    // TEMPORAL: PARA TESTING — Cambiar a false para reactivar el temporizador
    // =======================================================================
    private static final boolean TIMER_DESACTIVADO = false;
    // =======================================================================

    private GameState state;
    private Parser parser;
    private OxygenTimer timer;
    private EngineListener listener;
    private boolean juegoTerminado = false;

    public interface EngineListener {
        void onMessage(String msg);
        void onOxygenUpdate(int oxigeno);
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
        crearMapa();
        iniciarTimer(100);
        listener.onRoomChange(state.habitacionActual);
        listener.onMessage("Despiertas en la " + state.habitacionActual.getNombre() + ". " + state.habitacionActual.getDescripcion());
        listener.onMessage("El soporte vital esta fallando. Debes llegar al Modulo de Escape.");
    }

    private void crearMapa() {
        Stanza criogenia = new Stanza("crio", "Criogenia", "Una sala fria con capsulas vacias. Una puerta da al sur.");
        Stanza pasillo = new Stanza("pasillo", "Pasillo Central", "Un largo pasillo oscuro. Puertas al norte, sur, este y oeste.");
        Stanza control = new Stanza("control", "Sala de Control", "Monitores parpadeantes. Puedes ir al oeste o al este.");
        Stanza ingenieria = new Stanza("ing", "Ingenieria", "Motores ruidosos. Un pasaje va al este y otro al sur.");
        Stanza comunicaciones = new Stanza("com", "Comunicaciones", "Equipos de radio estropeados. Una terminal parece funcionar. Salida al norte.");
        Stanza laboratorio = new Stanza("lab", "Laboratorio", "Muestras biologicas destruidas. Salida al oeste.");
        Stanza escape = new Stanza("escape", "Modulo de Escape", "La unica salvacion. Salida al norte.");

        criogenia.setSalida("sur", pasillo);
        pasillo.setSalida("norte", criogenia);
        pasillo.setSalida("este", control);
        pasillo.setSalida("oeste", ingenieria);
        pasillo.setSalida("sur", comunicaciones);
        
        control.setSalida("oeste", pasillo);
        control.setSalida("este", laboratorio);
        laboratorio.setSalida("oeste", control);
        
        ingenieria.setSalida("este", pasillo);
        ingenieria.setSalida("sur", escape);
        escape.setSalida("norte", ingenieria);
        comunicaciones.setSalida("norte", pasillo);

        // Objetos
        criogenia.getObjetos().add(new Item("tarjeta", "Tarjeta", "Tarjeta de acceso de seguridad."));
        ingenieria.getObjetos().add(new Item("llave", "Llave Inglesa", "Herramienta pesada para reparar cosas."));
        control.getObjetos().add(new Item("tanque", "Tanque de Oxigeno", "Proporciona 60 segundos extra de aire."));

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
            case GUARDAR:
                guardar();
                break;
            case CARGAR:
                cargar();
                break;
            case AYUDA:
                listener.onMessage("Comandos: ir [n/s/e/o], mirar, tomar [obj], usar [obj], inv, hackear, guardar, cargar");
                break;
            default:
                listener.onMessage("No entiendo ese comando.");
        }
    }

    private void mover(String dir) {
        Stanza siguiente = state.habitacionActual.getSalida(dir);
        if (siguiente != null) {
            state.habitacionActual = siguiente;
            listener.onRoomChange(state.habitacionActual);
            listener.onMessage("Te mueves al " + dir + ". Llegaste a: " + state.habitacionActual.getNombre());
        } else {
            listener.onMessage("No puedes ir en esa direccion.");
        }
    }

    private void mirar() {
        listener.onMessage(state.habitacionActual.getDescripcion());
        List<Item> items = state.habitacionActual.getObjetos().getElementos();
        if (!items.isEmpty()) {
            listener.onMessage("Ves los siguientes objetos aqui:");
            for (Item i : items) {
                listener.onMessage("- " + i.getNombre());
            }
        }
    }

    private void tomar(String nombreItem) {
        List<Item> filtrados = state.habitacionActual.getObjetos().filtrarPorNombre(nombreItem);
        if (!filtrados.isEmpty()) {
            Item item = filtrados.get(0);
            state.habitacionActual.getObjetos().remove(item);
            state.inventario.add(item);
            listener.onMessage("Tomaste: " + item.getNombre());
            listener.onInventoryChange(state.inventario);
        } else {
            listener.onMessage("No veo ese objeto aqui.");
        }
    }

    private void usar(String nombreItem) {
        List<Item> filtrados = state.inventario.filtrarPorNombre(nombreItem);
        if (filtrados.isEmpty()) {
            listener.onMessage("No tienes ese objeto.");
            return;
        }
        Item item = filtrados.get(0);
        
        if (item.getId().equals("tanque")) {
            timer.addOxigeno(60);
            state.inventario.remove(item);
            listener.onMessage("Has usado el tanque. +60 segundos de oxigeno.");
            listener.onInventoryChange(state.inventario);
        } else if (item.getId().equals("llave") && state.habitacionActual.getId().equals("escape")) {
            listener.onMessage("Usas la llave inglesa para reparar la consola del modulo.");
            listener.onMessage("¡SISTEMA EN LINEA! Has escapado de la estacion. ¡GANASTE!");
            juegoTerminado = true;
            if (timer != null) timer.detener();
            DBManager.guardarPuntuacion("Jugador1", timer.getOxigenoRestante());
            listener.onGameOver(true);
        } else {
            listener.onMessage("No puedes usar eso aqui o no tiene efecto.");
        }
    }

    private void mostrarInventario() {
        List<Item> items = state.inventario.getElementos();
        if (items.isEmpty()) {
            listener.onMessage("Tu inventario esta vacio.");
        } else {
            listener.onMessage("Llevas:");
            for (Item i : items) {
                listener.onMessage("- " + i.getNombre());
            }
        }
    }

    private void hackear() {
        if (state.habitacionActual.getId().equals("comunicaciones")) {
            listener.onMessage("Hackeando terminal de red...");
            String msg = NetworkTerminal.getMessageFromServer();
            listener.onMessage(msg);
        } else if (state.habitacionActual.getId().equals("control")) {
            listener.onMessage("Accediendo a la base de datos local...");
            String log = DBManager.getRandomLog();
            listener.onMessage("LOG: " + log);
        } else {
            listener.onMessage("No hay terminales utiles aqui.");
        }
    }

    private void guardar() {
        if (timer != null) state.oxigenoRestante = timer.getOxigenoRestante();
        if (SaveLoadManager.guardar(state)) {
            listener.onMessage("Partida guardada.");
        } else {
            listener.onMessage("Error al guardar.");
        }
    }

    private void cargar() {
        GameState cargado = SaveLoadManager.cargar();
        if (cargado != null) {
            this.state = cargado;
            iniciarTimer(state.oxigenoRestante);
            listener.onRoomChange(state.habitacionActual);
            listener.onInventoryChange(state.inventario);
            listener.onMessage("Partida cargada exitosamente.");
        } else {
            listener.onMessage("Error al cargar la partida.");
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
            listener.onMessage("Te has quedado sin oxigeno... Fin del juego.");
            listener.onGameOver(false);
        }
    }
}
