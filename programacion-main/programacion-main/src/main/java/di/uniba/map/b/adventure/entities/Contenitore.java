package di.uniba.map.b.adventure.entities;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contenedor generico que usa un Set para evitar elementos duplicados.
 * Sirve tanto para el inventario del jugador como para los objetos de cada habitacion.
 */
public class Contenitore<T extends ObjetoJuego> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Set<T> elementos;

    public Contenitore() {
        this.elementos = new LinkedHashSet<>();
    }

    public void add(T elemento) {
        elementos.add(elemento);
    }

    public void remove(T elemento) {
        elementos.remove(elemento);
    }

    public Set<T> getElementos() {
        return elementos;
    }

    public List<T> filtrarPorNombre(String filtro) {
        return elementos.stream()
            .filter(e -> e.getNombre().toLowerCase().contains(filtro.toLowerCase()))
            .collect(Collectors.toList());
    }
}
