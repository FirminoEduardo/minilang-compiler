package codegen;

import ir.Instruction;
import ir.OpCode;

import java.util.ArrayList;
import java.util.List;

public class BytecodeGenerator {

    public List<BytecodeInstruction> generate(List<Instruction> tacCode) {
        List<BytecodeInstruction> bytecode = new ArrayList<>();

        for (Instruction instr : tacCode) {
            translate(instr, bytecode);
        }

        bytecode.add(new BytecodeInstruction(BytecodeOp.HALT, null));
        return bytecode;
    }

    private void translate(Instruction instr, List<BytecodeInstruction> out) {
        switch (instr.opCode) {

            // result = arg1
            case ASSIGN -> {
                loadValue(instr.arg1, out);
                out.add(new BytecodeInstruction(BytecodeOp.STORE, instr.result));
            }

            // result = arg1 OP arg2
            case ADD, SUB, MUL, DIV,
                 EQ, NEQ, LT, GT,
                 AND, OR -> {
                loadValue(instr.arg1, out);
                loadValue(instr.arg2, out);
                out.add(new BytecodeInstruction(toBytecodeOp(instr.opCode), null));
                out.add(new BytecodeInstruction(BytecodeOp.STORE, instr.result));
            }

            // result = OP arg1
            case NEG, NOT -> {
                loadValue(instr.arg1, out);
                out.add(new BytecodeInstruction(toBytecodeOp(instr.opCode), null));
                out.add(new BytecodeInstruction(BytecodeOp.STORE, instr.result));
            }

            case LABEL    -> out.add(new BytecodeInstruction(BytecodeOp.LABEL, instr.result));
            case GOTO     -> out.add(new BytecodeInstruction(BytecodeOp.JUMP,  instr.result));

            // IF_FALSE cond GOTO label
            case IF_FALSE -> {
                loadValue(instr.result, out);
                out.add(new BytecodeInstruction(BytecodeOp.JUMP_IF_FALSE, instr.arg1));
            }

            case PRINT -> {
                loadValue(instr.result, out);
                out.add(new BytecodeInstruction(BytecodeOp.PRINT, null));
            }

            case READ -> out.add(new BytecodeInstruction(BytecodeOp.READ, instr.result));
        }
    }

    // Literal: número, true/false, string entre aspas
    // Variável: qualquer outro token (x, t0, flag ...)
    private void loadValue(String value, List<BytecodeInstruction> out) {
        if (isLiteral(value)) {
            out.add(new BytecodeInstruction(BytecodeOp.PUSH, value));
        } else {
            out.add(new BytecodeInstruction(BytecodeOp.LOAD, value));
        }
    }

    private boolean isLiteral(String value) {
        if (value == null) return false;
        if (value.equals("true") || value.equals("false")) return true;
        if (value.startsWith("\"")) return true;
        try { Integer.parseInt(value); return true; }
        catch (NumberFormatException e) { return false; }
    }

    private BytecodeOp toBytecodeOp(OpCode op) {
        return switch (op) {
            case ADD -> BytecodeOp.ADD;
            case SUB -> BytecodeOp.SUB;
            case MUL -> BytecodeOp.MUL;
            case DIV -> BytecodeOp.DIV;
            case EQ  -> BytecodeOp.EQ;
            case NEQ -> BytecodeOp.NEQ;
            case LT  -> BytecodeOp.LT;
            case GT  -> BytecodeOp.GT;
            case AND -> BytecodeOp.AND;
            case OR  -> BytecodeOp.OR;
            case NEG -> BytecodeOp.NEG;
            case NOT -> BytecodeOp.NOT;
            default  -> throw new RuntimeException("OpCode não mapeável: " + op);
        };
    }
}