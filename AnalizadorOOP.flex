/* ============================================================
   AnalizadorOOP.flex
   Analizador Léxico — Lenguaje OOP basado en Java
   Herramienta: JFlex 1.9+
   Compilar:
       jflex AnalizadorOOP.flex          -> genera AnalizadorOOPLexer.java
       javac AnalizadorOOPLexer.java AnalizadorSintacticoOOP.java Main.java
       java  Main
   ============================================================ */

/* ──────────────────────────────────────────────────────────────
   SECCIÓN 1 — Opciones y declaraciones de la clase generada
   ────────────────────────────────────────────────────────────── */
%%

%class    AnalizadorOOPLexer
%type     Token
%unicode
%line
%column
%public

%{
    /* Método auxiliar para construir un Token con posición */
    private Token token(TipoToken tipo) {
        return new Token(tipo, yytext(), yyline + 1, yycolumn + 1);
    }
%}

/* ──────────────────────────────────────────────────────────────
   SECCIÓN 2 — Definiciones (macros de expresiones regulares)
   ────────────────────────────────────────────────────────────── */

/* --- Bloques básicos --- */
Letra          = [a-zA-Z]
Digito         = [0-9]
GuionBajo      = "_"
EspacioBlanco  = [ \t\r\n]+

/* --- Identificador: empieza con letra, luego letras/dígitos/guión bajo --- */
Identificador  = {Letra}({Letra}|{Digito}|{GuionBajo})*

/* --- Números de 8 bits ---
     Entero  : 0 .. 255  (se valida semánticamente; léxicamente cualquier dígito)
     Flotante: dígitos . dígitos                                              */
EnteroLit      = {Digito}+
FlotanteLit    = {Digito}+"."{Digito}+

/* --- Literal de cadena (entre comillas dobles, sin salto de línea) --- */
CadenaLit      = \"[^\"\n]*\"

/* --- Literal de carácter (entre comillas simples, un solo carácter) --- */
CharLit        = \'([^\'\n\\]|\\.)\'

/* --- Comentarios --- */
ComentLinea    = "//"[^\n]*
ComentBloque   = "/*"([^*]|\*+[^/*])*\*+"/"

/* ──────────────────────────────────────────────────────────────
   SECCIÓN 3 — Reglas léxicas
   ────────────────────────────────────────────────────────────── */
%%

/* ── Espacios y comentarios (se ignoran) ─────────────────────── */
{EspacioBlanco}   { /* ignorar */ }
{ComentLinea}     { /* ignorar */ }
{ComentBloque}    { /* ignorar */ }

/* ── Palabras reservadas — tipos de datos ─────────────────────── */
"int"             { return token(TipoToken.KW_INT);     }
"float"           { return token(TipoToken.KW_FLOAT);   }
"double"          { return token(TipoToken.KW_DOUBLE);  }
"boolean"         { return token(TipoToken.KW_BOOLEAN); }
"char"            { return token(TipoToken.KW_CHAR);    }
"String"          { return token(TipoToken.KW_STRING);  }
"void"            { return token(TipoToken.KW_VOID);    }

/* ── Palabras reservadas — clases y herencia ─────────────────── */
"class"           { return token(TipoToken.KW_CLASS);   }
"extends"         { return token(TipoToken.KW_EXTENDS); }

/* ── Palabras reservadas — control de acceso ─────────────────── */
"private"         { return token(TipoToken.KW_PRIVATE);   }
"public"          { return token(TipoToken.KW_PUBLIC);    }
"protected"       { return token(TipoToken.KW_PROTECTED); }

/* ── Palabras reservadas — estructuras de control ─────────────── */
"if"              { return token(TipoToken.KW_IF);     }
"else"            { return token(TipoToken.KW_ELSE);   }
"while"           { return token(TipoToken.KW_WHILE);  }
"do"              { return token(TipoToken.KW_DO);     }
"switch"          { return token(TipoToken.KW_SWITCH); }

/* ── Palabras reservadas — otras ─────────────────────────────── */
"main"            { return token(TipoToken.KW_MAIN);   }
"new"             { return token(TipoToken.KW_NEW);    }
"true"            { return token(TipoToken.KW_TRUE);   }
"false"           { return token(TipoToken.KW_FALSE);  }
"return"          { return token(TipoToken.KW_RETURN); }

/* ── Estructuras genéricas (List, ArrayList) ─────────────────── */
"List"            { return token(TipoToken.KW_LIST);        }
"ArrayList"       { return token(TipoToken.KW_ARRAYLIST);   }

/* ── Operadores ──────────────────────────────────────────────── */
"="               { return token(TipoToken.OP_ASIG);   }
"=="              { return token(TipoToken.OP_EQ);     }
"!="              { return token(TipoToken.OP_NEQ);    }
"<"               { return token(TipoToken.OP_LT);     }
">"               { return token(TipoToken.OP_GT);     }
"<="              { return token(TipoToken.OP_LTE);    }
">="              { return token(TipoToken.OP_GTE);    }

/* ── Delimitadores y separadores ─────────────────────────────── */
"{"               { return token(TipoToken.LLAVE_IZQ);  }
"}"               { return token(TipoToken.LLAVE_DER);  }
"("               { return token(TipoToken.PAREN_IZQ);  }
")"               { return token(TipoToken.PAREN_DER);  }
"["               { return token(TipoToken.CORCH_IZQ);  }
"]"               { return token(TipoToken.CORCH_DER);  }
";"               { return token(TipoToken.PUNTO_COMA); }
","               { return token(TipoToken.COMA);       }
"."               { return token(TipoToken.PUNTO);      }

/* ── Diamante genérico (List<Tipo>) ──────────────────────────── */
"<"               { return token(TipoToken.OP_LT);      }
">"               { return token(TipoToken.OP_GT);      }

/* ── Literales ──────────────────────────────────────────────── */
{FlotanteLit}     { return token(TipoToken.LIT_FLOTANTE); }
{EnteroLit}       { return token(TipoToken.LIT_ENTERO);   }
{CadenaLit}       { return token(TipoToken.LIT_CADENA);   }
{CharLit}         { return token(TipoToken.LIT_CHAR);     }

/* ── Identificador (DEBE ir después de todas las palabras clave) */
{Identificador}   { return token(TipoToken.IDENTIFICADOR); }

/* ── Error léxico ────────────────────────────────────────────── */
[^]               {
    System.err.printf("[ERROR LÉXICO] Línea %d, Col %d: carácter no reconocido '%s'%n",
                      yyline + 1, yycolumn + 1, yytext());
    return token(TipoToken.DESCONOCIDO);
}
