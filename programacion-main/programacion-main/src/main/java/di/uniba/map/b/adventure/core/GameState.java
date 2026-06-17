package di.uniba.map.b.adventure.core;

import di.uniba.map.b.adventure.entities.Contenitore;
import di.uniba.map.b.adventure.entities.ObjetoJuego;
import di.uniba.map.b.adventure.entities.Stanza;
import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public Stanza habitacionActual;
    public Contenitore<ObjetoJuego> inventario;
    public int oxigenoRestante;
    public List<Stanza> mapa;
    public boolean vitrinaRota;
    public boolean puzzleLabResuelto;
    public boolean ingDoorUnlocked;
    
    public GameState(Stanza habitacionActual, Contenitore<ObjetoJuego> inventario, int oxigenoRestante, List<Stanza> mapa) {
        this.habitacionActual = habitacionActual;
        this.inventario = inventario;
        this.oxigenoRestante = oxigenoRestante;
        this.mapa = mapa;
        this.vitrinaRota = false;
        this.puzzleLabResuelto = false;
        this.ingDoorUnlocked = false;
    }
}
