package codegen;

public enum BytecodeOp {
    // Pilha
    PUSH,           // empurra literal
    LOAD,           // empurra valor de variável
    STORE,          // salva topo da pilha em variável

    // Aritméticos
    ADD, SUB, MUL, DIV,

    // Relacionais
    EQ, NEQ, LT, GT,

    // Lógicos
    AND, OR, NEG, NOT,

    // Controle de fluxo
    LABEL,
    JUMP,
    JUMP_IF_FALSE,

    // E/S
    PRINT,
    READ,

    HALT
}