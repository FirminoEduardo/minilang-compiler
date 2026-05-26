package semantic;

import ast.Node;
import ast.nodes.*;

import java.util.List;

public class SemanticAnalyzer {

    private SymbolTable currentScope;

    public SemanticAnalyzer() {
        // Escopo global (sem pai)
        this.currentScope = new SymbolTable(null);
    }

    // -------------------------------------------------------
    // Ponto de entrada
    // -------------------------------------------------------

    public void analyze(ProgramNode program) {
        for (Node statement : program.statements) {
            analyzeStatement(statement);
        }
    }

    // -------------------------------------------------------
    // Statements
    // -------------------------------------------------------

    private void analyzeStatement(Node node) {
        switch (node) {
            case VarDeclNode v  -> analyzeVarDecl(v);
            case AssignNode  a  -> analyzeAssign(a);
            case IfNode      i  -> analyzeIf(i);
            case WhileNode   w  -> analyzeWhile(w);
            case PrintNode   p  -> analyzeExpression(p.expression);
            case ReadNode    r  -> analyzeRead(r);
            default -> throw new SemanticError(
                    "Nó desconhecido na análise semântica: " + node.getClass().getSimpleName()
            );
        }
    }

    private void analyzeVarDecl(VarDeclNode node) {
        // Só "int" e "bool" são tipos válidos
        if (!node.type.equals("int") && !node.type.equals("bool")) {
            throw new SemanticError("Tipo inválido: '" + node.type + "'");
        }
        currentScope.declare(node.name, node.type);
    }

    private void analyzeAssign(AssignNode node) {
        // Variável deve estar declarada
        String varType  = currentScope.lookup(node.name);
        String exprType = analyzeExpression(node.expression);

        if (!varType.equals(exprType)) {
            throw new SemanticError(
                    "Tipo incompatível na atribuição de '" + node.name +
                            "': esperado '" + varType + "', encontrado '" + exprType + "'"
            );
        }
    }

    private void analyzeIf(IfNode node) {
        String condType = analyzeExpression(node.condition);
        if (!condType.equals("bool")) {
            throw new SemanticError(
                    "Condição do 'if' deve ser bool, encontrado '" + condType + "'"
            );
        }
        analyzeBlock(node.thenBranch);
        analyzeBlock(node.elseBranch);
    }

    private void analyzeWhile(WhileNode node) {
        String condType = analyzeExpression(node.condition);
        if (!condType.equals("bool")) {
            throw new SemanticError(
                    "Condição do 'while' deve ser bool, encontrado '" + condType + "'"
            );
        }
        analyzeBlock(node.body);
    }

    private void analyzeRead(ReadNode node) {
        // Variável de destino deve estar declarada
        currentScope.lookup(node.name);
    }

    // Abre um novo escopo para blocos { }
    private void analyzeBlock(List<Node> statements) {
        SymbolTable blockScope = new SymbolTable(currentScope);
        SymbolTable previous   = currentScope;
        currentScope = blockScope;

        for (Node stmt : statements) {
            analyzeStatement(stmt);
        }

        currentScope = previous; // restaura escopo anterior
    }

    // -------------------------------------------------------
    // Expressões — retorna o tipo inferido ("int" ou "bool")
    // -------------------------------------------------------

    private String analyzeExpression(Node node) {
        return switch (node) {
            case NumberNode     n -> "int";
            case BoolNode       b -> "bool";
            case StringNode     s -> "string";
            case IdentifierNode i -> currentScope.lookup(i.name);

            case BinOpNode b -> analyzeBinOp(b);
            case UnaryOpNode u -> analyzeUnaryOp(u);

            default -> throw new SemanticError(
                    "Expressão desconhecida: " + node.getClass().getSimpleName()
            );
        };
    }

    private String analyzeBinOp(BinOpNode node) {
        String left  = analyzeExpression(node.left);
        String right = analyzeExpression(node.right);

        return switch (node.operator) {
            // Aritméticos: int OP int → int
            case "+", "-", "*", "/" -> {
                requireType(left,  "int", "Operando esquerdo de '" + node.operator + "'");
                requireType(right, "int", "Operando direito de '"  + node.operator + "'");
                yield "int";
            }
            // Comparação: int OP int → bool
            case "<", ">" -> {
                requireType(left,  "int", "Operando esquerdo de '" + node.operator + "'");
                requireType(right, "int", "Operando direito de '"  + node.operator + "'");
                yield "bool";
            }
            // Igualdade: mesmo tipo em ambos os lados → bool
            case "==", "!=" -> {
                if (!left.equals(right)) {
                    throw new SemanticError(
                            "Operandos de '" + node.operator +
                                    "' devem ser do mesmo tipo: '" + left + "' vs '" + right + "'"
                    );
                }
                yield "bool";
            }
            // Lógicos: bool OP bool → bool
            case "&&", "||" -> {
                requireType(left,  "bool", "Operando esquerdo de '" + node.operator + "'");
                requireType(right, "bool", "Operando direito de '"  + node.operator + "'");
                yield "bool";
            }
            default -> throw new SemanticError("Operador desconhecido: '" + node.operator + "'");
        };
    }

    private String analyzeUnaryOp(UnaryOpNode node) {
        String operandType = analyzeExpression(node.operand);
        return switch (node.operator) {
            case "-" -> {
                requireType(operandType, "int", "Operando de '-' unário");
                yield "int";
            }
            case "!" -> {
                requireType(operandType, "bool", "Operando de '!'");
                yield "bool";
            }
            default -> throw new SemanticError("Operador unário desconhecido: '" + node.operator + "'");
        };
    }

    // -------------------------------------------------------
    // Utilitário
    // -------------------------------------------------------

    private void requireType(String actual, String expected, String context) {
        if (!actual.equals(expected)) {
            throw new SemanticError(
                    context + ": esperado '" + expected + "', encontrado '" + actual + "'"
            );
        }
    }
}