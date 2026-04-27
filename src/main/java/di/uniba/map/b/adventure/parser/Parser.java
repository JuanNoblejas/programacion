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
            case "ir":
            case "mover":
            case "caminar":
            case "norte":
            case "sur":
            case "este":
            case "oeste":
                tipo = ComandoType.IR;
                if (argumento.isEmpty() && !verbo.equals("ir") && !verbo.equals("mover") && !verbo.equals("caminar")) {
                    argumento = verbo; // if they just type "norte"
                }
                break;
            case "tomar":
            case "coger":
            case "recoger":
            case "agarrar":
                tipo = ComandoType.TOMAR;
                break;
            case "usar":
            case "utilizar":
                tipo = ComandoType.USAR;
                break;
            case "inventario":
            case "inv":
            case "i":
                tipo = ComandoType.INVENTARIO;
                break;
            case "mirar":
            case "observar":
            case "examinar":
                tipo = ComandoType.MIRAR;
                break;
            case "hackear":
            case "conectar":
                tipo = ComandoType.HACKEAR;
                break;
            case "guardar":
            case "save":
                tipo = ComandoType.GUARDAR;
                break;
            case "cargar":
            case "load":
                tipo = ComandoType.CARGAR;
                break;
            case "ayuda":
            case "help":
                tipo = ComandoType.AYUDA;
                break;
            default:
                tipo = ComandoType.DESCONOCIDO;
                break;
        }
        
        return new ResultadoParser(tipo, argumento);
    }
}
