package ir;

public class Instruction {

    public final OpCode opCode;
    public final String result;  // destino (ou label, ou variável de E/S)
    public final String arg1;    // operando 1 (pode ser null)
    public final String arg2;    // operando 2 (pode ser null)

    public Instruction(OpCode opCode, String result, String arg1, String arg2) {
        this.opCode = opCode;
        this.result = result;
        this.arg1   = arg1;
        this.arg2   = arg2;
    }

    @Override
    public String toString() {
        return switch (opCode) {
            case ASSIGN   -> result + " = " + arg1;
            case ADD      -> result + " = " + arg1 + " + "  + arg2;
            case SUB      -> result + " = " + arg1 + " - "  + arg2;
            case MUL      -> result + " = " + arg1 + " * "  + arg2;
            case DIV      -> result + " = " + arg1 + " / "  + arg2;
            case EQ       -> result + " = " + arg1 + " == " + arg2;
            case NEQ      -> result + " = " + arg1 + " != " + arg2;
            case LT       -> result + " = " + arg1 + " < "  + arg2;
            case GT       -> result + " = " + arg1 + " > "  + arg2;
            case AND      -> result + " = " + arg1 + " && " + arg2;
            case OR       -> result + " = " + arg1 + " || " + arg2;
            case NEG      -> result + " = -" + arg1;
            case NOT      -> result + " = !" + arg1;
            case LABEL    -> "LABEL " + result;
            case GOTO     -> "GOTO "  + result;
            case IF_FALSE -> "IF_FALSE " + result + " GOTO " + arg1;
            case PRINT    -> "PRINT " + result;
            case READ     -> "READ "  + result;
        };
    }
}