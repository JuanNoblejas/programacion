package di.uniba.map.b.adventure.entities;

/**
 * Subclase concreta de ObjetoJuego que representa herramientas
 * utilizables para reparar o romper cosas en la estacion.
 */
public class Herramienta extends ObjetoJuego {
    private static final long serialVersionUID = 1L;

    public Herramienta(String id, String nombre, String descripcion) {
        super(id, nombre, descripcion);
    }

    @Override
    public void usar() {
        System.out.println("You use the tool: " + getNombre());
    }
}
