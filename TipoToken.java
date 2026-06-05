public enum TipoToken {
    // Tipos de datos
    KW_INT, KW_FLOAT, KW_DOUBLE, KW_BOOLEAN, KW_CHAR, KW_STRING, KW_VOID,
    // Clases y herencia
    KW_CLASS, KW_EXTENDS,
    // Control de acceso
    KW_PRIVATE, KW_PUBLIC, KW_PROTECTED,
    // Control de flujo
    KW_IF, KW_ELSE, KW_WHILE, KW_DO, KW_SWITCH,
    // Otras palabras reservadas
    KW_MAIN, KW_NEW, KW_TRUE, KW_FALSE, KW_RETURN,
    // Estructuras genéricas
    KW_LIST, KW_ARRAYLIST,
    // Operadores
    OP_ASIG, OP_EQ, OP_NEQ, OP_LT, OP_GT, OP_LTE, OP_GTE,
    // Delimitadores
    LLAVE_IZQ, LLAVE_DER, PAREN_IZQ, PAREN_DER, CORCH_IZQ, CORCH_DER,
    PUNTO_COMA, COMA, PUNTO,
    // Literales
    LIT_ENTERO, LIT_FLOTANTE, LIT_CADENA, LIT_CHAR,
    // Identificador genérico
    IDENTIFICADOR,
    // Fin de archivo y error
    EOF, DESCONOCIDO
}
