import java.io.*;
import java.util.*;

/**
 * Punto de entrada: usa el lexer generado por JFlex (AnalizadorOOPLexer)
 * para tokenizar código OOP basado en Java y luego ejecuta el parser.
 *
 * Compilar:
 *   javac -encoding UTF-8 TipoToken.java Token.java AnalizadorOOPLexer.java Main.java
 * Ejecutar:
 *   java -Dfile.encoding=UTF-8 Main
 */
public class Main {

    static final String CODIGO_EJEMPLO =
        "// 1. Instanciación de objeto\n" +
        "Lampara lampara = new Lampara();\n" +
        "\n" +
        "// 2. Invocación de método sin parámetros\n" +
        "lampara.interruptor();\n" +
        "\n" +
        "// 3. Invocación con un parámetro (String)\n" +
        "lampara.setColor(String color);\n" +
        "\n" +
        "// 3b. Invocación con un parámetro (int)\n" +
        "lampara.setColor(int color);\n" +
        "\n" +
        "// 4. Invocación con múltiples parámetros\n" +
        "alumno.actualizarDatos(\"Luis\", 22, \"Ingenieria\");\n" +
        "\n" +
        "// 5. Asignación de valor mediante atributo\n" +
        "String color = miAuto.color;\n" +
        "\n" +
        "// 6. Lista (estructura genérica)\n" +
        "List<Estudiante> lista = new ArrayList<>();\n";

    static void linea(String c, int n) { System.out.println("  " + c.repeat(n)); }

    public static void main(String[] args) throws Exception {
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        System.setOut(out);
        Scanner sc = new Scanner(new InputStreamReader(System.in, "UTF-8"));

        System.out.println();
        linea("═", 66);
        System.out.println("  ANALIZADOR OOP — Lexer generado por JFlex");
        linea("═", 66);
        System.out.println("  1) Código de ejemplo (6 construcciones OOP)");
        System.out.println("  2) Ingresar código personalizado");
        linea("─", 66);
        System.out.print("  Opción: ");

        String op = sc.nextLine().trim();
        String codigo;

        if (op.equals("1")) {
            codigo = CODIGO_EJEMPLO;
        } else {
            System.out.println("  Ingrese código (línea vacía para terminar):");
            StringBuilder sb = new StringBuilder();
            String ln;
            while (!(ln = sc.nextLine()).isEmpty()) sb.append(ln).append("\n");
            codigo = sb.toString();
        }

        System.out.println();
        linea("─", 66);
        System.out.println("  CÓDIGO FUENTE");
        linea("─", 66);
        for (String l : codigo.split("\n", -1)) System.out.println("  " + l);

        // ── Análisis Léxico via JFlex ─────────────────────────────
        AnalizadorOOPLexer lexer = new AnalizadorOOPLexer(new StringReader(codigo));
        List<Token> tokens = new ArrayList<>();
        Token t;
        while ((t = lexer.yylex()) != null) tokens.add(t);
        tokens.add(new Token(TipoToken.EOF, "<EOF>", 0, 0));

        System.out.println();
        linea("═", 66);
        System.out.println("  TOKENS RECONOCIDOS  (generados por JFlex)");
        linea("═", 66);
        System.out.printf("  %-18s %-24s %-5s %s%n", "TIPO", "LEXEMA", "L", "C");
        linea("─", 66);
        for (Token tk : tokens)
            if (tk.tipo != TipoToken.EOF) System.out.println("  " + tk);
        linea("─", 66);

        // ── Análisis Sintáctico ───────────────────────────────────
        System.out.println();
        AnalizadorSintacticoOOP.imprimirBNF();
        System.out.println();
        AnalizadorSintacticoOOP.Parser parser = new AnalizadorSintacticoOOP.Parser(tokens);
        parser.parsear();
        parser.imprimirResultados();

        // ── Tabla de Símbolos ─────────────────────────────────────
        AnalizadorSintacticoOOP.imprimirTablaSimbolos(tokens);

        sc.close();
    }
}
