package ir;

import ast.Node;
import ast.nodes.*;

import java.util.ArrayList;
import java.util.List;

public class IRGenerator {

    private final List<Instruction> instructions = new ArrayList<>();
    private int tempCount  = 0;  // contador de temporários: t0, t1, ...
    private int labelCount = 0;  // contador de labels: L0, L1, ...

    // -------------------------------------------------------
    // Ponto de entrada
    // -------------------------------------------------------

    public List<Instruction> generate(ProgramNode program) {
        for (Node statement : program.statements) {
            generateStatement(statement);
        }
        return instructions;
    }

    // -------------------------------------------------------
    // Statements
    // -------------------------------------------------------

    private void generateStatement(Node node) {
        switch (node) {
            case VarDeclNode v  -> { /* declarações não geram IR */ }
            case AssignNode  a  -> generateAssign(a);
            case IfNode      i  -> generateIf(i);
            case WhileNode   w  -> generateWhile(w);
            case PrintNode   p  -> generatePrint(p);
            case ReadNode    r  -> generateRead(r);
            default -> throw new RuntimeException(
                    "Nó desconhecido na geração de IR: " + node.getClass().getSimpleName()
            );
        }
    }

    private void generateAssign(AssignNode node) {
        String exprResult = generateExpression(node.expression);
        emit(new Instruction(OpCode.ASSIGN, node.name, exprResult, null));
    }

    private void generateIf(IfNode node) {
        String cond    = generateExpression(node.condition);
        String labelElse = newLabel();  // pula para o else (ou fim)
        String labelEnd  = newLabel();  // fim do if

        // Se condição for falsa, pula para o else
        emit(new Instruction(OpCode.IF_FALSE, cond, labelElse, null));

        // Bloco then
        for (Node stmt : node.thenBranch) generateStatement(stmt);
        emit(new Instruction(OpCode.GOTO, labelEnd, null, null));

        // Bloco else
        emit(new Instruction(OpCode.LABEL, labelElse, null, null));
        for (Node stmt : node.elseBranch) generateStatement(stmt);

        emit(new Instruction(OpCode.LABEL, labelEnd, null, null));
    }

    private void generateWhile(WhileNode node) {
        String labelStart = newLabel();  // início do loop
        String labelEnd   = newLabel();  // saída do loop

        emit(new Instruction(OpCode.LABEL, labelStart, null, null));

        String cond = generateExpression(node.condition);
        emit(new Instruction(OpCode.IF_FALSE, cond, labelEnd, null));

        for (Node stmt : node.body) generateStatement(stmt);
        emit(new Instruction(OpCode.GOTO, labelStart, null, null));

        emit(new Instruction(OpCode.LABEL, labelEnd, null, null));
    }

    private void generatePrint(PrintNode node) {
        String val = generateExpression(node.expression);
        emit(new Instruction(OpCode.PRINT, val, null, null));
    }

    private void generateRead(ReadNode node) {
        emit(new Instruction(OpCode.READ, node.name, null, null));
    }

    // -------------------------------------------------------
    // Expressões — retorna o nome do temporário ou variável
    // que contém o resultado
    // -------------------------------------------------------

    private String generateExpression(Node node) {
        return switch (node) {
            case NumberNode     n -> String.valueOf(n.value);
            case BoolNode       b -> b.value ? "true" : "false";
            case StringNode     s -> "\"" + s.value + "\"";
            case IdentifierNode i -> i.name;

            case BinOpNode  b -> generateBinOp(b);
            case UnaryOpNode u -> generateUnaryOp(u);

            default -> throw new RuntimeException(
                    "Expressão desconhecida na geração de IR: " + node.getClass().getSimpleName()
            );
        };
    }

    private String generateBinOp(BinOpNode node) {
        String left  = generateExpression(node.left);
        String right = generateExpression(node.right);
        String temp  = newTemp();

        OpCode op = switch (node.operator) {
            case "+"  -> OpCode.ADD;
            case "-"  -> OpCode.SUB;
            case "*"  -> OpCode.MUL;
            case "/"  -> OpCode.DIV;
            case "==" -> OpCode.EQ;
            case "!=" -> OpCode.NEQ;
            case "<"  -> OpCode.LT;
            case ">"  -> OpCode.GT;
            case "&&" -> OpCode.AND;
            case "||" -> OpCode.OR;
            default   -> throw new RuntimeException("Operador desconhecido: " + node.operator);
        };

        emit(new Instruction(op, temp, left, right));
        return temp;
    }

    private String generateUnaryOp(UnaryOpNode node) {
        String operand = generateExpression(node.operand);
        String temp    = newTemp();

        OpCode op = switch (node.operator) {
            case "-" -> OpCode.NEG;
            case "!" -> OpCode.NOT;
            default  -> throw new RuntimeException("Operador unário desconhecido: " + node.operator);
        };

        emit(new Instruction(op, temp, operand, null));
        return temp;
    }

    // -------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------

    private void emit(Instruction instruction) {
        instructions.add(instruction);
    }

    private String newTemp() {
        return "t" + tempCount++;
    }

    private String newLabel() {
        return "L" + labelCount++;
    }
}