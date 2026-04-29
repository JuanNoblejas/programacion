package di.uniba.map.b.adventure.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Stanza implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String nombre;
    private String descripcion;
    private Map<String, Stanza> salidas;
    private Contenitore<Item> objetosEnHabitacion;

    public Stanza(String id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.salidas = new HashMap<>();
        this.objetosEnHabitacion = new Contenitore<>();
    }

    public void setSalida(String direccion, Stanza habitacion) {
        salidas.put(direccion.toLowerCase(), habitacion);
    }

    public Stanza getSalida(String direccion) {
        return salidas.get(direccion.toLowerCase());
    }

    public Map<String, Stanza> getSalidas() {
        return salidas;
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Contenitore<Item> getObjetos() { return objetosEnHabitacion; }
    public String getId() { return id; }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Stanza stanza = (Stanza) obj;
        return id.equals(stanza.id);
    }
}
