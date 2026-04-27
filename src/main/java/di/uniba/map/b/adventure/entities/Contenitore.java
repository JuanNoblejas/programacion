package di.uniba.map.b.adventure.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Contenitore<T extends ObjetoJuego> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> elementos;

    public Contenitore() {
        this.elementos = new ArrayList<>();
    }

    public void add(T elemento) {
        elementos.add(elemento);
    }

    public void remove(T elemento) {
        elementos.remove(elemento);
    }

    public List<T> getElementos() {
        return elementos;
    }

    public List<T> filtrarPorNombre(String filtro) {
        return elementos.stream()
            .filter(e -> e.getNombre().toLowerCase().contains(filtro.toLowerCase()))
            .collect(Collectors.toList());
    }
}
