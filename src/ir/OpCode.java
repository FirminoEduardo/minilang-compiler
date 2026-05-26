package ir;

public enum OpCode {
    // Atribuição
    ASSIGN,       // x = y

    // Aritméticos
    ADD,          // x = y + z
    SUB,          // x = y - z
    MUL,          // x = y * z
    DIV,          // x = y / z

    // Relacionais / lógicos — resultado é bool
    EQ,           // x = y == z
    NEQ,          // x = y != z
    LT,           // x = y < z
    GT,           // x = y > z
    AND,          // x = y && z
    OR,           // x = y || z

    // Unários
    NEG,          // x = -y
    NOT,          // x = !y

    // Controle de fluxo
    LABEL,        // LABEL Ln
    GOTO,         // GOTO Ln
    IF_FALSE,     // IF_FALSE x GOTO Ln

    // E/S
    PRINT,        // PRINT x
    READ,         // READ x
}