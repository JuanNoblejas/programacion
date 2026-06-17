package di.uniba.map.b.adventure.entities;

/**
 * Enum que representa las direcciones cardinales de movimiento
 * en la estacion espacial.
 */
public enum Direction {
    NORTH, SOUTH, EAST, WEST;

    /**
     * Convierte un String a Direction de forma segura.
     * @param s la cadena a convertir (ej. "north", "SOUTH")
     * @return la Direction correspondiente o null si no es valida
     */
    public static Direction fromString(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
