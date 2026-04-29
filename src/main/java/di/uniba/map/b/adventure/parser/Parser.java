package di.uniba.map.b.adventure.parser;

public class Parser {
    
    public enum ComandoType {
        IR, TOMAR, USAR, INVENTARIO, MIRAR, HACKEAR, GUARDAR, CARGAR, AYUDA, DESCONOCIDO
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
        
        String[] partes = input.trim().toLowerCase().split("\\s+", 2);
        String verbo = partes[0];
        String argumento = partes.length > 1 ? partes[1] : "";
        
        ComandoType tipo = ComandoType.DESCONOCIDO;
        
        switch (verbo) {
            case "go": case "move": case "walk":
            case "ir": case "mover": case "caminar":
                tipo = ComandoType.IR;
                argumento = translateDir(argumento);
                break;
            case "north": case "south": case "east": case "west":
            case "n": case "s": case "e": case "w":
            case "norte": case "sur": case "este": case "oeste":
                tipo = ComandoType.IR;
                argumento = translateDir(verbo);
                break;
            case "take": case "pick": case "grab":
            case "tomar": case "coger": case "recoger": case "agarrar":
                tipo = ComandoType.TOMAR;
                break;
            case "use": case "usar": case "utilizar":
                tipo = ComandoType.USAR;
                break;
            case "inventory": case "inv": case "i": case "inventario":
                tipo = ComandoType.INVENTARIO;
                break;
            case "look": case "examine": case "inspect":
            case "mirar": case "observar": case "examinar":
                tipo = ComandoType.MIRAR;
                break;
            case "hack": case "connect":
            case "hackear": case "conectar":
                tipo = ComandoType.HACKEAR;
                break;
            case "save": case "guardar":
                tipo = ComandoType.GUARDAR;
                break;
            case "load": case "cargar":
                tipo = ComandoType.CARGAR;
                break;
            case "help": case "ayuda":
                tipo = ComandoType.AYUDA;
                break;
            default:
                tipo = ComandoType.DESCONOCIDO;
                break;
        }
        
        return new ResultadoParser(tipo, argumento);
    }

    private String translateDir(String dir) {
        switch (dir.toLowerCase()) {
            case "n": case "north": case "norte": return "north";
            case "s": case "south": case "sur":   return "south";
            case "e": case "east":  case "este":  return "east";
            case "w": case "west":  case "oeste": return "west";
            default: return dir;
        }
    }
}
