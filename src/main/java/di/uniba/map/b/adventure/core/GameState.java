package di.uniba.map.b.adventure.core;

import di.uniba.map.b.adventure.entities.Contenitore;
import di.uniba.map.b.adventure.entities.Item;
import di.uniba.map.b.adventure.entities.Stanza;
import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public Stanza habitacionActual;
    public Contenitore<Item> inventario;
    public int oxigenoRestante;
    public List<Stanza> mapa;
    
    public GameState(Stanza habitacionActual, Contenitore<Item> inventario, int oxigenoRestante, List<Stanza> mapa) {
        this.habitacionActual = habitacionActual;
        this.inventario = inventario;
        this.oxigenoRestante = oxigenoRestante;
        this.mapa = mapa;
    }
}
