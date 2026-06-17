package di.uniba.map.b.adventure.entities;

public class Item extends ObjetoJuego {
    private static final long serialVersionUID = 1L;

    public Item(String id, String nombre, String descripcion) {
        super(id, nombre, descripcion);
    }

    @Override
    public void usar() {
        System.out.println("Has usado: " + getNombre());
    }
}
