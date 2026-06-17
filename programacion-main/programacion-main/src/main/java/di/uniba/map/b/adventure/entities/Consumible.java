package di.uniba.map.b.adventure.entities;

/**
 * Subclase concreta de ObjetoJuego que representa objetos consumibles
 * de un solo uso, como tanques de oxigeno.
 */
public class Consumible extends ObjetoJuego {
    private static final long serialVersionUID = 1L;
    private boolean consumido = false;

    public Consumible(String id, String nombre, String descripcion) {
        super(id, nombre, descripcion);
    }

    @Override
    public void usar() {
        if (!consumido) {
            consumido = true;
            System.out.println("You consumed: " + getNombre());
        } else {
            System.out.println(getNombre() + " has already been used.");
        }
    }

    public boolean isConsumido() { return consumido; }
}
