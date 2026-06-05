import java.util.*;
import java.io.*;

/**
 * Analizador Léxico para Bloques de Código 1 (Imperativo) y 2 (OOP en español).
 *
 * Compilar : javac -encoding UTF-8 AnalizadorLexico.java
 * Ejecutar : java  -Dfile.encoding=UTF-8 AnalizadorLexico
 */
public class AnalizadorLexico {

    // =========================================================================
    //  TIPOS DE TOKEN
    // =========================================================================
    enum TipoToken {
        // ── Palabras reservadas (control de flujo, tipos, OOP) ───────────────
        PALABRA_RESERVADA,
        // ── Identificadores ──────────────────────────────────────────────────
        IDENTIFICADOR,
        // ── Literales ────────────────────────────────────────────────────────
        LITERAL_ENTERO,
        LITERAL_FLOTANTE,
        LITERAL_CADENA,
        LITERAL_BOOLEANO,
        // ── Operadores relacionales (palabras) ────────────────────────────────
        RELOP_MAYOR,        // mayor_que
        RELOP_MENOR,        // menor_que
        RELOP_IGUAL,        // equals
        // ── Operadores de asignación y aritméticos ────────────────────────────
        OPERADOR_ASIG,      // =
        OPERADOR_SUMA,      // +
        OPERADOR_RESTA,     // -
        // ── Delimitadores ────────────────────────────────────────────────────
        PAREN_IZQ,          // (
        PAREN_DER,          // )
        LLAVE_IZQ,          // {
        LLAVE_DER,          // }
        PUNTO_COMA,         // ;
        SEPARADOR,          // /  (separador de cláusulas en bucles for/para)
        PUNTO,              // .
        // ── Error léxico ─────────────────────────────────────────────────────
        DESCONOCIDO
    }

    // =========================================================================
    //  TOKEN
    // =========================================================================
    static class Token {
        final TipoToken tipo;
        final String    lexema;
        final int       linea;

        Token(TipoToken tipo, String lexema, int linea) {
            this.tipo   = tipo;
            this.lexema = lexema;
            this.linea  = linea;
        }

        @Override
        public String toString() {
            return String.format("      %-24s → \"%s\"", tipo, lexema);
        }
    }

    // =========================================================================
    //  CONJUNTOS LÉXICOS
    // =========================================================================

    /** Palabras reservadas de ambos bloques. */
    static final Set<String> PALABRAS_RESERVADAS = new LinkedHashSet<>(Arrays.asList(
        // Bloque 1
        "int", "flotante", "if", "for",
        // Bloque 2
        "clase", "atributo", "método", "si", "si_no",
        "retornar", "para", "objeto", "nuevo", "entero", "cadena"
    ));

    /** Literales de tipo booleano. */
    static final Set<String> LITERALES_BOOLEANOS = new HashSet<>(Arrays.asList(
        "verdadero", "falso"
    ));

    // =========================================================================
    //  TABLA DE SÍMBOLOS  (lexema → tipo de token)
    // =========================================================================
    static final Map<String, TipoToken> tablaSimbolos = new LinkedHashMap<>();

    // =========================================================================
    //  TOKENIZADOR
    // =========================================================================
    static List<Token> tokenizar(String fuente) {
        List<Token> tokens = new ArrayList<>();
        String[] lineas    = fuente.split("\n", -1);

        for (int nLinea = 0; nLinea < lineas.length; nLinea++) {
            String linea = lineas[nLinea];
            int    i     = 0;

            while (i < linea.length()) {
                char c = linea.charAt(i);

                // ── Espacios en blanco ─────────────────────────────────────────
                if (Character.isWhitespace(c)) { i++; continue; }

                // ── Cadena literal ─────────────────────────────────────────────
                if (c == '"') {
                    StringBuilder sb = new StringBuilder("\"");
                    i++;
                    while (i < linea.length() && linea.charAt(i) != '"') {
                        sb.append(linea.charAt(i++));
                    }
                    if (i < linea.length()) { sb.append('"'); i++; }
                    agregar(tokens, TipoToken.LITERAL_CADENA, sb.toString(), nLinea + 1);
                    continue;
                }

                // ── Número (entero o flotante) ─────────────────────────────────
                if (Character.isDigit(c)) {
                    StringBuilder sb         = new StringBuilder();
                    boolean       esFlotante  = false;
                    while (i < linea.length()
                            && (Character.isDigit(linea.charAt(i)) || linea.charAt(i) == '.')) {
                        if (linea.charAt(i) == '.') esFlotante = true;
                        sb.append(linea.charAt(i++));
                    }
                    TipoToken tt = esFlotante ? TipoToken.LITERAL_FLOTANTE : TipoToken.LITERAL_ENTERO;
                    agregar(tokens, tt, sb.toString(), nLinea + 1);
                    continue;
                }

                // ── Palabra: reservada / booleano / relop / identificador ──────
                if (Character.isLetter(c) || c == '_') {
                    StringBuilder sb = new StringBuilder();
                    while (i < linea.length()
                            && (Character.isLetterOrDigit(linea.charAt(i))
                                || linea.charAt(i) == '_')) {
                        sb.append(linea.charAt(i++));
                    }
                    String    palabra = sb.toString();
                    TipoToken tt;

                    if      (PALABRAS_RESERVADAS.contains(palabra))  tt = TipoToken.PALABRA_RESERVADA;
                    else if (LITERALES_BOOLEANOS.contains(palabra))  tt = TipoToken.LITERAL_BOOLEANO;
                    else if (palabra.equals("mayor_que"))            tt = TipoToken.RELOP_MAYOR;
                    else if (palabra.equals("menor_que"))            tt = TipoToken.RELOP_MENOR;
                    else if (palabra.equals("equals"))               tt = TipoToken.RELOP_IGUAL;
                    else                                              tt = TipoToken.IDENTIFICADOR;

                    agregar(tokens, tt, palabra, nLinea + 1);
                    continue;
                }

                // ── Caracteres simples ─────────────────────────────────────────
                TipoToken tt;
                switch (c) {
                    case '=': tt = TipoToken.OPERADOR_ASIG;  break;
                    case '+': tt = TipoToken.OPERADOR_SUMA;  break;
                    case '-': tt = TipoToken.OPERADOR_RESTA; break;
                    case '(': tt = TipoToken.PAREN_IZQ;      break;
                    case ')': tt = TipoToken.PAREN_DER;      break;
                    case '{': tt = TipoToken.LLAVE_IZQ;      break;
                    case '}': tt = TipoToken.LLAVE_DER;      break;
                    case ';': tt = TipoToken.PUNTO_COMA;     break;
                    case '/': tt = TipoToken.SEPARADOR;      break;
                    case '.': tt = TipoToken.PUNTO;          break;
                    default:  tt = TipoToken.DESCONOCIDO;    break;
                }
                agregar(tokens, tt, String.valueOf(c), nLinea + 1);
                i++;
            }
        }
        return tokens;
    }

    /** Añade un token a la lista y lo registra en la tabla de símbolos. */
    static void agregar(List<Token> lista, TipoToken tt, String lexema, int linea) {
        lista.add(new Token(tt, lexema, linea));
        if (tt != TipoToken.DESCONOCIDO) {
            tablaSimbolos.putIfAbsent(lexema, tt);
        } else {
            System.out.printf("  [ERROR LÉXICO] Línea %d: lexema '%s' no reconocido%n", linea, lexema);
        }
    }

    // =========================================================================
    //  IMPRESIÓN — GRAMÁTICA / TOKENS POR LÍNEA
    // =========================================================================
    static void imprimirGramatica(List<Token> tokens, String[] lineas) {
        System.out.println();
        linea("═", 66);
        System.out.println("  GRAMÁTICA — TOKENS RECONOCIDOS POR LÍNEA");
        linea("═", 66);

        Map<Integer, List<Token>> porLinea = new LinkedHashMap<>();
        for (Token t : tokens) porLinea.computeIfAbsent(t.linea, k -> new ArrayList<>()).add(t);

        for (Map.Entry<Integer, List<Token>> entrada : porLinea.entrySet()) {
            int         n  = entrada.getKey();
            List<Token> tl = entrada.getValue();
            if (n <= lineas.length && !lineas[n - 1].trim().isEmpty()) {
                System.out.printf("%n  Línea %2d │ %s%n", n, lineas[n - 1].trim());
                System.out.print("  Producción │ ");
                for (Token t : tl) System.out.print(t.tipo + " ");
                System.out.println();
                System.out.println("  Tokens     │");
                for (Token t : tl) System.out.println(t);
            }
        }
        linea("─", 66);
    }

    // =========================================================================
    //  IMPRESIÓN — TABLA DE SÍMBOLOS
    // =========================================================================
    static void imprimirTablaSimbolos() {
        System.out.println();
        System.out.println("  ╔══════╦══════════════════════════════════╦══════════════════════════╗");
        System.out.println("  ║  #   ║  Lexema                          ║  Tipo Token              ║");
        System.out.println("  ╠══════╬══════════════════════════════════╬══════════════════════════╣");
        int n = 1;
        for (Map.Entry<String, TipoToken> e : tablaSimbolos.entrySet()) {
            System.out.printf("  ║  %-4d║  %-32s║  %-24s║%n",
                              n++, e.getKey(), e.getValue());
        }
        System.out.println("  ╚══════╩══════════════════════════════════╩══════════════════════════╝");
    }

    // =========================================================================
    //  GRAMÁTICAS FORMALES (BNF)
    // =========================================================================
    static void imprimirGramaticaFormal(int bloque) {
        System.out.println();
        linea("═", 66);
        System.out.println("  GRAMÁTICA FORMAL BNF — Bloque " + bloque);
        linea("═", 66);
        if (bloque == 1) {
            System.out.println(
                "  <programa>   → <sentencia>*\n" +
                "  <sentencia>  → <declaracion> | <if_stmt> | <for_stmt> | <asignacion>\n" +
                "  <declaracion>→ <tipo> IDENT '=' <expr> ';'\n" +
                "  <tipo>       → 'int' | 'flotante'\n" +
                "  <if_stmt>    → 'if' '(' <cond> ')' '{' <cuerpo> '}'\n" +
                "  <cond>       → <expr> <relop> <expr>\n" +
                "  <relop>      → 'mayor_que' | 'menor_que' | 'equals'\n" +
                "  <for_stmt>   → 'for' '(' <for_ini> '/' <cond> '/' <for_act> ')' '{' <cuerpo> '}'\n" +
                "  <for_ini>    → <tipo> IDENT '=' <expr>\n" +
                "  <for_act>    → IDENT '=' <expr>\n" +
                "  <asignacion> → IDENT '=' <expr> ';'?\n" +
                "  <cuerpo>     → <sentencia>*\n" +
                "  <expr>       → <term> (('+' | '-') <term>)*\n" +
                "  <term>       → IDENT | LIT_ENTERO | LIT_FLOTANTE | LIT_CADENA"
            );
        } else {
            System.out.println(
                "  <programa>   → <clase>*\n" +
                "  <clase>      → 'clase' IDENT '{' <miembro>* '}'\n" +
                "  <miembro>    → <atributo> | <metodo>\n" +
                "  <atributo>   → 'atributo' IDENT '=' <valor> ';'\n" +
                "  <valor>      → LIT_CADENA | LIT_ENTERO | LIT_FLOTANTE | LIT_BOOLEANO\n" +
                "  <metodo>     → 'método' IDENT '(' <params>? ')' '{' <cuerpo> '}'\n" +
                "  <params>     → IDENT (',' IDENT)*\n" +
                "  <cuerpo>     → <sentencia>*\n" +
                "  <sentencia>  → <asignacion> | <if_stmt> | <for_stmt>\n" +
                "               | <retorno>    | <llamada>\n" +
                "  <asignacion> → (<tipo_d> IDENT | <acceso>) '=' <expr> ';'?\n" +
                "  <tipo_d>     → 'entero' | 'cadena' | 'objeto'\n" +
                "  <if_stmt>    → 'si' '(' <cond> ')' '{' <cuerpo> '}' <si_no>?\n" +
                "  <si_no>      → 'si_no' '{' <cuerpo> '}'\n" +
                "  <for_stmt>   → 'para' '(' <for_ini> '/' <cond> '/' <for_act> ')' '{' <cuerpo> '}'\n" +
                "  <for_ini>    → 'entero' IDENT '=' <expr>\n" +
                "  <for_act>    → IDENT '=' <expr>\n" +
                "  <retorno>    → 'retornar' <expr> ';'\n" +
                "  <llamada>    → <acceso> '(' <args>? ')' ';'\n" +
                "  <acceso>     → IDENT ('.' IDENT)*\n" +
                "  <cond>       → <expr> <relop> <expr>\n" +
                "  <relop>      → 'mayor_que' | 'menor_que' | 'equals'\n" +
                "  <expr>       → <term> (('+' | '-') <term>)*\n" +
                "  <term>       → <acceso> | LIT_ENTERO | LIT_FLOTANTE | LIT_CADENA\n" +
                "               | 'nuevo' IDENT '(' ')'"
            );
        }
        linea("─", 66);
    }

    // =========================================================================
    //  CÓDIGO FUENTE PRE-CARGADO
    // =========================================================================
    static final String BLOQUE1 =
        "int a = 10;\n" +
        "int b_1 = 1.0;\n" +
        "flotante c2 = 3.1;\n" +
        "\n" +
        "if (a mayor_que b_1 ){\n" +
        "  c2 = c2 + 1;\n" +
        "}\n" +
        "\n" +
        "if(c2 equals b_1){r = \"cadena\"}\n" +
        "\n" +
        "for(int i = 0 / i menor_que VARIABLE_D / i=i+1){\n" +
        "    a = VARIABLE_D - varE;\n" +
        "}";

    static final String BLOQUE2 =
        "clase Persona {\n" +
        "    atributo nombre = \"Ana\";\n" +
        "    atributo edad = 20;\n" +
        "    atributo activo = verdadero;\n" +
        "\n" +
        "    método incrementarEdad() {\n" +
        "        edad = edad + 1;\n" +
        "    }\n" +
        "\n" +
        "    método compararEdad(otro) {\n" +
        "        si (edad mayor_que otro.edad) {\n" +
        "            retornar \"más grande\";\n" +
        "        }\n" +
        "        si_no {\n" +
        "            retornar \"más joven\";\n" +
        "        }\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "clase Programa {\n" +
        "    método principal() {\n" +
        "        objeto p1 = nuevo Persona();\n" +
        "        objeto p2 = nuevo Persona();\n" +
        "        p2.edad = 25;\n" +
        "\n" +
        "        resultado = p1.compararEdad(p2);\n" +
        "\n" +
        "        para (entero i = 0 / i menor_que 3 / i = i + 1) {\n" +
        "            p1.incrementarEdad();\n" +
        "        }\n" +
        "\n" +
        "        si (p1.edad equals p2.edad) {\n" +
        "            cadena r = \"Edades iguales\";\n" +
        "        }\n" +
        "    }\n" +
        "}";

    // =========================================================================
    //  UTILIDADES
    // =========================================================================
    static void linea(String car, int n) {
        System.out.println("  " + car.repeat(n));
    }

    // =========================================================================
    //  MAIN
    // =========================================================================
    public static void main(String[] args) throws Exception {
        PrintStream salida = new PrintStream(System.out, true, "UTF-8");
        System.setOut(salida);
        Scanner sc = new Scanner(new InputStreamReader(System.in, "UTF-8"));

        System.out.println();
        linea("═", 66);
        System.out.println("  ANALIZADOR LÉXICO — Bloques de Código 1 y 2");
        linea("═", 66);
        System.out.println("  1) Bloque de Código 1  (Imperativo / Procedural)");
        System.out.println("  2) Bloque de Código 2  (Orientado a Objetos)");
        System.out.println("  3) Ingresar código personalizado");
        linea("─", 66);
        System.out.print("  Opción: ");

        int opcion = Integer.parseInt(sc.nextLine().trim());

        String codigo;
        int    bloque;

        switch (opcion) {
            case 1:
                codigo = BLOQUE1;
                bloque = 1;
                break;
            case 2:
                codigo = BLOQUE2;
                bloque = 2;
                break;
            default:
                bloque = 0;
                System.out.println("  Ingrese el código (línea vacía para finalizar):");
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

        // Resetear tabla para este análisis
        tablaSimbolos.clear();

        List<Token> tokens = tokenizar(codigo);
        String[]    lineas = codigo.split("\n", -1);

        imprimirGramatica(tokens, lineas);

        if (bloque == 1 || bloque == 2) imprimirGramaticaFormal(bloque);

        System.out.println();
        linea("═", 66);
        System.out.println("  TABLA DE SÍMBOLOS");
        linea("═", 66);
        imprimirTablaSimbolos();

        sc.close();
    }
}
