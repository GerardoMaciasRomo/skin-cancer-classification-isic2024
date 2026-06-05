public class Token {
    public final TipoToken tipo;
    public final String    lexema;
    public final int       linea;
    public final int       columna;

    public Token(TipoToken tipo, String lexema, int linea, int columna) {
        this.tipo    = tipo;
        this.lexema  = lexema;
        this.linea   = linea;
        this.columna = columna;
    }

    @Override public String toString() {
        return String.format("%-18s %-22s  L%-3d C%d",
                             tipo, "\"" + lexema + "\"", linea, columna);
    }
}
