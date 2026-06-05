import java.util.*;
import java.io.*;

/**
 * Parser descendente recursivo para lenguaje OOP basado en Java.
 * Usa las clases externas Token y TipoToken (generadas junto al lexer JFlex).
 *
 * Standalone (sin JFlex):
 *   javac -encoding UTF-8 TipoToken.java Token.java AnalizadorSintacticoOOP.java
 *   java  -Dfile.encoding=UTF-8 AnalizadorSintacticoOOP
 *
 * Con JFlex (via Main.java):
 *   javac -encoding UTF-8 TipoToken.java Token.java AnalizadorOOPLexer.java \
 *         AnalizadorSintacticoOOP.java Main.java
 *   java  -Dfile.encoding=UTF-8 Main
 */
public class AnalizadorSintacticoOOP {

    // =========================================================
    //  LEXER MANUAL (equivalente al .flex, sin requerir JFlex)
    // =========================================================
    static class Lexer {

        private final String fuente;
        private int pos   = 0;
        private int linea = 1;
        private int col   = 1;

        private static final Map<String, TipoToken> KEYWORDS = new LinkedHashMap<>();
        static {
            KEYWORDS.put("int",       TipoToken.KW_INT);
            KEYWORDS.put("float",     TipoToken.KW_FLOAT);
            KEYWORDS.put("double",    TipoToken.KW_DOUBLE);
            KEYWORDS.put("boolean",   TipoToken.KW_BOOLEAN);
            KEYWORDS.put("char",      TipoToken.KW_CHAR);
            KEYWORDS.put("String",    TipoToken.KW_STRING);
            KEYWORDS.put("void",      TipoToken.KW_VOID);
            KEYWORDS.put("class",     TipoToken.KW_CLASS);
            KEYWORDS.put("extends",   TipoToken.KW_EXTENDS);
            KEYWORDS.put("private",   TipoToken.KW_PRIVATE);
            KEYWORDS.put("public",    TipoToken.KW_PUBLIC);
            KEYWORDS.put("protected", TipoToken.KW_PROTECTED);
            KEYWORDS.put("if",        TipoToken.KW_IF);
            KEYWORDS.put("else",      TipoToken.KW_ELSE);
            KEYWORDS.put("while",     TipoToken.KW_WHILE);
            KEYWORDS.put("do",        TipoToken.KW_DO);
            KEYWORDS.put("switch",    TipoToken.KW_SWITCH);
            KEYWORDS.put("main",      TipoToken.KW_MAIN);
            KEYWORDS.put("new",       TipoToken.KW_NEW);
            KEYWORDS.put("true",      TipoToken.KW_TRUE);
            KEYWORDS.put("false",     TipoToken.KW_FALSE);
            KEYWORDS.put("return",    TipoToken.KW_RETURN);
            KEYWORDS.put("List",      TipoToken.KW_LIST);
            KEYWORDS.put("ArrayList", TipoToken.KW_ARRAYLIST);
        }

        Lexer(String fuente) { this.fuente = fuente; }

        private char actual()    { return pos < fuente.length() ? fuente.charAt(pos) : '\0'; }
        private char siguiente() { return (pos+1) < fuente.length() ? fuente.charAt(pos+1) : '\0'; }
        private void avanzar()   { if (actual()=='\n'){linea++;col=1;}else col++; pos++; }

        List<Token> tokenizar() {
            List<Token> lista = new ArrayList<>();
            while (pos < fuente.length()) {
                Token t = nextToken();
                if (t != null) lista.add(t);
            }
            lista.add(new Token(TipoToken.EOF, "<EOF>", linea, col));
            return lista;
        }

        private Token nextToken() {
            while (pos < fuente.length() && Character.isWhitespace(actual())) avanzar();
            if (pos >= fuente.length()) return null;
            int l = linea, c = col;
            char ch = actual();

            if (ch=='/' && siguiente()=='/') { while(pos<fuente.length()&&actual()!='\n') avanzar(); return null; }
            if (ch=='/' && siguiente()=='*') {
                avanzar(); avanzar();
                while(pos<fuente.length()){ if(actual()=='*'&&siguiente()=='/'){avanzar();avanzar();break;} avanzar(); }
                return null;
            }
            if (ch=='"') {
                StringBuilder sb=new StringBuilder("\""); avanzar();
                while(pos<fuente.length()&&actual()!='"'&&actual()!='\n'){sb.append(actual());avanzar();}
                if(actual()=='"'){sb.append('"');avanzar();}
                return new Token(TipoToken.LIT_CADENA, sb.toString(), l, c);
            }
            if (ch=='\'') {
                StringBuilder sb=new StringBuilder("'"); avanzar();
                while(pos<fuente.length()&&actual()!='\''&&actual()!='\n'){sb.append(actual());avanzar();}
                if(actual()=='\''){sb.append('\'');avanzar();}
                return new Token(TipoToken.LIT_CHAR, sb.toString(), l, c);
            }
            if (Character.isDigit(ch)) {
                StringBuilder sb=new StringBuilder(); boolean esF=false;
                while(pos<fuente.length()&&(Character.isDigit(actual())||actual()=='.')){
                    if(actual()=='.'){if(esF)break;esF=true;} sb.append(actual());avanzar();
                }
                return new Token(esF?TipoToken.LIT_FLOTANTE:TipoToken.LIT_ENTERO, sb.toString(), l, c);
            }
            if (Character.isLetter(ch)||ch=='_') {
                StringBuilder sb=new StringBuilder();
                while(pos<fuente.length()&&(Character.isLetterOrDigit(actual())||actual()=='_')){sb.append(actual());avanzar();}
                String p=sb.toString();
                return new Token(KEYWORDS.getOrDefault(p,TipoToken.IDENTIFICADOR), p, l, c);
            }
            if(ch=='='&&siguiente()=='='){avanzar();avanzar();return new Token(TipoToken.OP_EQ, "==",l,c);}
            if(ch=='!'&&siguiente()=='='){avanzar();avanzar();return new Token(TipoToken.OP_NEQ,"!=",l,c);}
            if(ch=='<'&&siguiente()=='='){avanzar();avanzar();return new Token(TipoToken.OP_LTE,"<=",l,c);}
            if(ch=='>'&&siguiente()=='='){avanzar();avanzar();return new Token(TipoToken.OP_GTE,">=",l,c);}
            avanzar();
            switch(ch){
                case '=': return new Token(TipoToken.OP_ASIG,    "=",  l,c);
                case '<': return new Token(TipoToken.OP_LT,      "<",  l,c);
                case '>': return new Token(TipoToken.OP_GT,      ">",  l,c);
                case '{': return new Token(TipoToken.LLAVE_IZQ,  "{",  l,c);
                case '}': return new Token(TipoToken.LLAVE_DER,  "}",  l,c);
                case '(': return new Token(TipoToken.PAREN_IZQ,  "(",  l,c);
                case ')': return new Token(TipoToken.PAREN_DER,  ")",  l,c);
                case '[': return new Token(TipoToken.CORCH_IZQ,  "[",  l,c);
                case ']': return new Token(TipoToken.CORCH_DER,  "]",  l,c);
                case ';': return new Token(TipoToken.PUNTO_COMA, ";",  l,c);
                case ',': return new Token(TipoToken.COMA,       ",",  l,c);
                case '.': return new Token(TipoToken.PUNTO,      ".",  l,c);
                default:
                    System.err.printf("[ERROR LÉXICO] L%d C%d: '%c'%n",l,c,ch);
                    return new Token(TipoToken.DESCONOCIDO, String.valueOf(ch), l, c);
            }
        }
    }

    // =========================================================
    //  PARSER — Descendente Recursivo
    //
    //  Gramática:
    //  programa         → sentencia*
    //  sentencia        → instanciacion | invocacion
    //                   | asignacionAtrib | declaracionLista
    //  instanciacion    → <tipo> IDENT '=' 'new' IDENT '(' ')' ';'
    //  invocacion       → IDENT '.' IDENT '(' <listaArgs>? ')' ';'
    //  listaArgs        → arg (',' arg)*
    //  arg              → <tipo> IDENT | LIT_CADENA | LIT_ENTERO | LIT_FLOTANTE | IDENT
    //  asignacionAtrib  → <tipo> IDENT '=' IDENT '.' IDENT ';'
    //  declaracionLista → 'List' '<' IDENT '>' IDENT '=' 'new' 'ArrayList' '<' '>' '(' ')' ';'
    //  tipo             → 'int'|'float'|'double'|'boolean'|'char'|'String'|'void'|IDENT
    // =========================================================
    public static class Parser {

        private final List<Token> tokens;
        private int cursor = 0;
        private final List<String> errores = new ArrayList<>();
        private final List<String> exitos  = new ArrayList<>();

        public Parser(List<Token> tokens) { this.tokens = tokens; }

        private Token actual() { return tokens.get(cursor); }

        private boolean verifica(TipoToken... tipos) {
            for (TipoToken t : tipos) if (actual().tipo == t) return true;
            return false;
        }

        private boolean esTipo() {
            return verifica(TipoToken.KW_INT, TipoToken.KW_FLOAT, TipoToken.KW_DOUBLE,
                            TipoToken.KW_BOOLEAN, TipoToken.KW_CHAR, TipoToken.KW_STRING,
                            TipoToken.KW_VOID, TipoToken.IDENTIFICADOR);
        }

        private boolean esKeywordTipo(Token t) {
            switch (t.tipo) {
                case KW_INT: case KW_FLOAT: case KW_DOUBLE:
                case KW_BOOLEAN: case KW_CHAR: case KW_STRING:
                case KW_VOID: return true;
                default:      return false;
            }
        }

        public void parsear() {
            while (actual().tipo != TipoToken.EOF) {
                int antes = cursor;
                if (!sentencia()) {
                    if (cursor == antes) {
                        errores.add(String.format(
                            "[ERROR SINTÁCTICO] L%d: sentencia no reconocida — <%s> \"%s\"",
                            actual().linea, actual().tipo, actual().lexema));
                        cursor++;
                    }
                }
            }
        }

        private boolean sentencia() {
            int ini = cursor;
            if (declaracionLista())   return true; cursor = ini;
            if (instanciacion())      return true; cursor = ini;
            if (asignacionAtributo()) return true; cursor = ini;
            if (invocacionMetodo())   return true; cursor = ini;
            return false;
        }

        // 1. Instanciación: Tipo var = new Tipo();
        private boolean instanciacion() {
            int ini = cursor;
            if (!esTipo()) return false;
            Token tipoTok = tokens.get(cursor++);
            if (actual().tipo != TipoToken.IDENTIFICADOR)        { cursor=ini; return false; }
            Token varTok  = tokens.get(cursor++);
            if (actual().tipo != TipoToken.OP_ASIG)              { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.KW_NEW)               { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.IDENTIFICADOR &&
                !esKeywordTipo(actual()))                         { cursor=ini; return false; }
            Token claseTok = tokens.get(cursor++);
            if (actual().tipo != TipoToken.PAREN_IZQ)            { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.PAREN_DER)            { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.PUNTO_COMA)           { cursor=ini; return false; }
            cursor++;
            exitos.add(String.format("[OK] Instanciación         →  %s %s = new %s();",
                                     tipoTok.lexema, varTok.lexema, claseTok.lexema));
            return true;
        }

        // 2. Invocación: obj.metodo(args?);
        private boolean invocacionMetodo() {
            int ini = cursor;
            if (actual().tipo != TipoToken.IDENTIFICADOR) return false;
            Token obj = tokens.get(cursor++);
            if (actual().tipo != TipoToken.PUNTO)   { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.IDENTIFICADOR) { cursor=ini; return false; }
            Token met = tokens.get(cursor++);
            if (actual().tipo != TipoToken.PAREN_IZQ)    { cursor=ini; return false; }
            cursor++;
            List<String> args = new ArrayList<>();
            while (actual().tipo != TipoToken.PAREN_DER && actual().tipo != TipoToken.EOF) {
                args.add(argumento());
                if (actual().tipo == TipoToken.COMA) cursor++;
            }
            if (actual().tipo != TipoToken.PAREN_DER)    { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.PUNTO_COMA)   { cursor=ini; return false; }
            cursor++;
            String desc;
            if (args.isEmpty())
                desc = String.format("[OK] Invocación sin params  →  %s.%s();", obj.lexema, met.lexema);
            else if (args.size()==1)
                desc = String.format("[OK] Invocación 1 parámetro →  %s.%s(%s);", obj.lexema, met.lexema, args.get(0));
            else
                desc = String.format("[OK] Invocación N parámetros→  %s.%s(%s);", obj.lexema, met.lexema, String.join(", ", args));
            exitos.add(desc);
            return true;
        }

        private String argumento() {
            StringBuilder sb = new StringBuilder();
            if (esTipo() && cursor+1 < tokens.size() &&
                tokens.get(cursor+1).tipo == TipoToken.IDENTIFICADOR) {
                sb.append(tokens.get(cursor++).lexema).append(" ");
                sb.append(tokens.get(cursor++).lexema);
            } else {
                sb.append(actual().lexema); cursor++;
            }
            return sb.toString();
        }

        // 3. Asignación por atributo: tipo var = obj.atrib;
        private boolean asignacionAtributo() {
            int ini = cursor;
            if (!esTipo()) return false;
            Token tipoTok = tokens.get(cursor++);
            if (actual().tipo != TipoToken.IDENTIFICADOR) { cursor=ini; return false; }
            Token varTok  = tokens.get(cursor++);
            if (actual().tipo != TipoToken.OP_ASIG)       { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.IDENTIFICADOR) { cursor=ini; return false; }
            Token obj  = tokens.get(cursor++);
            if (actual().tipo != TipoToken.PUNTO)         { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.IDENTIFICADOR) { cursor=ini; return false; }
            Token attr = tokens.get(cursor++);
            if (actual().tipo != TipoToken.PUNTO_COMA)    { cursor=ini; return false; }
            cursor++;
            exitos.add(String.format("[OK] Asignación atributo    →  %s %s = %s.%s;",
                                     tipoTok.lexema, varTok.lexema, obj.lexema, attr.lexema));
            return true;
        }

        // 4. Lista genérica: List<Tipo> var = new ArrayList<>();
        private boolean declaracionLista() {
            int ini = cursor;
            if (actual().tipo != TipoToken.KW_LIST)       return false;
            cursor++;
            if (actual().tipo != TipoToken.OP_LT)         { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.IDENTIFICADOR &&
                !esKeywordTipo(actual()))                  { cursor=ini; return false; }
            Token elemTok = tokens.get(cursor++);
            if (actual().tipo != TipoToken.OP_GT)         { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.IDENTIFICADOR) { cursor=ini; return false; }
            Token varTok  = tokens.get(cursor++);
            if (actual().tipo != TipoToken.OP_ASIG)       { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.KW_NEW)        { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.KW_ARRAYLIST)  { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.OP_LT)         { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.OP_GT)         { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.PAREN_IZQ)     { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.PAREN_DER)     { cursor=ini; return false; }
            cursor++;
            if (actual().tipo != TipoToken.PUNTO_COMA)    { cursor=ini; return false; }
            cursor++;
            exitos.add(String.format("[OK] Declaración lista      →  List<%s> %s = new ArrayList<>();",
                                     elemTok.lexema, varTok.lexema));
            return true;
        }

        public void imprimirResultados() {
            linea("═", 66);
            System.out.println("  RESULTADOS DEL ANÁLISIS SINTÁCTICO");
            linea("═", 66);
            if (exitos.isEmpty() && errores.isEmpty())
                System.out.println("  (sin sentencias reconocidas)");
            for (String e : exitos)  System.out.println("  " + e);
            if (!errores.isEmpty()) { System.out.println(); for (String e : errores) System.out.println("  " + e); }
            linea("─", 66);
            System.out.printf("  Sentencias válidas: %d   Errores: %d%n", exitos.size(), errores.size());
            linea("═", 66);
        }
    }

    // =========================================================
    //  TABLA DE SÍMBOLOS
    // =========================================================
    public static void imprimirTablaSimbolos(List<Token> tokens) {
        Map<String, TipoToken> tabla = new LinkedHashMap<>();
        for (Token t : tokens)
            if (t.tipo != TipoToken.EOF && t.tipo != TipoToken.DESCONOCIDO)
                tabla.putIfAbsent(t.lexema, t.tipo);
        System.out.println();
        linea("═", 66);
        System.out.println("  TABLA DE SÍMBOLOS");
        linea("═", 66);
        System.out.println("  ╔══════╦══════════════════════════════╦══════════════════════════╗");
        System.out.println("  ║  #   ║  Lexema                      ║  Tipo Token              ║");
        System.out.println("  ╠══════╬══════════════════════════════╬══════════════════════════╣");
        int n = 1;
        for (Map.Entry<String, TipoToken> e : tabla.entrySet())
            System.out.printf("  ║  %-4d║  %-28s║  %-24s║%n", n++, e.getKey(), e.getValue());
        System.out.println("  ╚══════╩══════════════════════════════╩══════════════════════════╝");
    }

    // =========================================================
    //  GRAMÁTICA FORMAL BNF
    // =========================================================
    public static void imprimirBNF() {
        linea("═", 66);
        System.out.println("  GRAMÁTICA FORMAL BNF");
        linea("═", 66);
        System.out.println(
            "  <programa>          → <sentencia>*\n\n" +
            "  <sentencia>         → <instanciacion> | <invocacion>\n" +
            "                      | <asignacionAtrib> | <declaracionLista>\n\n" +
            "  <instanciacion>     → <tipo> IDENT '=' 'new' IDENT '(' ')' ';'\n\n" +
            "  <invocacion>        → IDENT '.' IDENT '(' <listaArgs>? ')' ';'\n" +
            "  <listaArgs>         → <arg> (',' <arg>)*\n" +
            "  <arg>               → <tipo> IDENT | LIT_CADENA | LIT_ENTERO\n" +
            "                      | LIT_FLOTANTE | IDENT\n\n" +
            "  <asignacionAtrib>   → <tipo> IDENT '=' IDENT '.' IDENT ';'\n\n" +
            "  <declaracionLista>  → 'List' '<' IDENT '>' IDENT\n" +
            "                        '=' 'new' 'ArrayList' '<' '>' '(' ')' ';'\n\n" +
            "  <tipo>              → 'int'|'float'|'double'|'boolean'\n" +
            "                      | 'char'|'String'|'void'|IDENT"
        );
        linea("─", 66);
    }

    static void linea(String c, int n) { System.out.println("  " + c.repeat(n)); }

    // =========================================================
    //  MAIN (modo standalone, usa el Lexer manual)
    // =========================================================
    static final String CODIGO_EJEMPLO =
        "// 1. Instanciación de objeto\n" +
        "Lampara lampara = new Lampara();\n\n" +
        "// 2. Invocación sin parámetros\n" +
        "lampara.interruptor();\n\n" +
        "// 3. Invocación con 1 parámetro (String)\n" +
        "lampara.setColor(String color);\n\n" +
        "// 3b. Invocación con 1 parámetro (int)\n" +
        "lampara.setColor(int color);\n\n" +
        "// 4. Invocación con múltiples parámetros\n" +
        "alumno.actualizarDatos(\"Luis\", 22, \"Ingenieria\");\n\n" +
        "// 5. Asignación de valor mediante atributo\n" +
        "String color = miAuto.color;\n\n" +
        "// 6. Lista genérica\n" +
        "List<Estudiante> lista = new ArrayList<>();\n";

    public static void main(String[] args) throws Exception {
        PrintStream salida = new PrintStream(System.out, true, "UTF-8");
        System.setOut(salida);
        Scanner sc = new Scanner(new InputStreamReader(System.in, "UTF-8"));

        System.out.println();
        linea("═", 66);
        System.out.println("  ANALIZADOR OOP — modo standalone (Lexer manual)");
        linea("═", 66);
        System.out.println("  1) Código de ejemplo");
        System.out.println("  2) Código personalizado");
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

        Lexer lexer       = new Lexer(codigo);
        List<Token> tokens = lexer.tokenizar();

        System.out.println();
        linea("═", 66);
        System.out.println("  TOKENS RECONOCIDOS");
        linea("═", 66);
        System.out.printf("  %-18s %-24s %-5s %s%n","TIPO","LEXEMA","L","C");
        linea("─", 66);
        for (Token t : tokens) if (t.tipo != TipoToken.EOF) System.out.println("  " + t);
        linea("─", 66);

        System.out.println();
        imprimirBNF();
        System.out.println();

        Parser parser = new Parser(tokens);
        parser.parsear();
        parser.imprimirResultados();
        imprimirTablaSimbolos(tokens);

        sc.close();
    }
}
