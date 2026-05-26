package codegen;

public class BytecodeInstruction {

    public final BytecodeOp op;
    public final String operand; // null para instruções sem argumento

    public BytecodeInstruction(BytecodeOp op, String operand) {
        this.op      = op;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return operand != null ? op + " " + operand : op.toString();
    }
}