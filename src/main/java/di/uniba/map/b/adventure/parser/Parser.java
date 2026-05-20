package di.uniba.map.b.adventure.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    /**
     * Expresion regular con grupos de captura para aislar el verbo y el argumento.
     * Grupo 1: la primera palabra (verbo/comando).
     * Grupo 2: todo lo que sigue despues del primer espacio (argumento, opcional).
     */
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(\\w+)(?:\\s+(.+))?$");

    public enum ComandoType {
        IR, TOMAR, USAR, INVENTARIO, MIRAR, HACKEAR, GUARDAR, CARGAR, AYUDA,
        ROMPER, RESOLVER,
        DESCONOCIDO
    }
    
    public static class ResultadoParser {
        public ComandoType tipo;
        public String argumento;
        
        public ResultadoParser(ComandoType tipo, String argumento) {
            this.tipo = tipo;
            this.argumento = argumento;
        }
    }
    
    public ResultadoParser parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ResultadoParser(ComandoType.DESCONOCIDO, "");
        }
        
        Matcher matcher = COMMAND_PATTERN.matcher(input.trim().toLowerCase());
        
        if (!matcher.matches()) {
            return new ResultadoParser(ComandoType.DESCONOCIDO, "");
        }
        
        String verbo = matcher.group(1);
        String argumento = matcher.group(2) != null ? matcher.group(2).trim() : "";
        
        ComandoType tipo = ComandoType.DESCONOCIDO;
        
        switch (verbo) {
            case "go":
            case "move":
            case "walk":
            case "north":
            case "south":
            case "east":
            case "west":
                tipo = ComandoType.IR;
                if (argumento.isEmpty() && !verbo.equals("go") && !verbo.equals("move") && !verbo.equals("walk")) {
                    argumento = verbo;
                }
                break;
            case "take":
            case "pick":
            case "grab":
                tipo = ComandoType.TOMAR;
                break;
            case "use":
                tipo = ComandoType.USAR;
                break;
            case "inventory":
            case "inv":
            case "i":
                tipo = ComandoType.INVENTARIO;
                break;
            case "look":
            case "examine":
            case "observe":
                tipo = ComandoType.MIRAR;
                break;
            case "hack":
            case "connect":
                tipo = ComandoType.HACKEAR;
                break;
            case "save":
                tipo = ComandoType.GUARDAR;
                break;
            case "load":
                tipo = ComandoType.CARGAR;
                break;
            case "help":
                tipo = ComandoType.AYUDA;
                break;
            case "break":
            case "smash":
            case "hit":
                tipo = ComandoType.ROMPER;
                break;
            case "solve":
            case "answer":
                tipo = ComandoType.RESOLVER;
                break;
            default:
                tipo = ComandoType.DESCONOCIDO;
                break;
        }
        
        return new ResultadoParser(tipo, argumento);
    }
}
