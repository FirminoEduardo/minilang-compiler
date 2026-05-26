package lexer;

public enum TokenType {
    // Palavras reservadas
    INT, BOOL, IF, ELSE, WHILE, PRINT, READ, TRUE, FALSE,

    // Identificadores e literais
    IDENTIFIER,   // nomes de variáveis
    NUMBER,       // ex: 42
    STRING,       // ex: "hello"

    // Operadores aritméticos
    PLUS,         // +
    MINUS,        // -
    STAR,         // *
    SLASH,        // /

    // Operadores relacionais / lógicos
    EQUAL_EQUAL,  // ==
    BANG_EQUAL,   // !=
    LESS,         //
    GREATER,      // >
    AND,          // &&
    OR,           // ||
    BANG,         // !

    // Atribuição
    EQUAL,        // =

    // Delimitadores
    LPAREN,       // (
    RPAREN,       // )
    LBRACE,       // {
    RBRACE,       // }
    SEMICOLON,    // ;
    COMMA,        // ,

    // Controle
    EOF           // fim do arquivo
}