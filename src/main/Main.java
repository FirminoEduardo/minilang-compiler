package main;

import ast.Node;
import ast.nodes.*;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import semantic.SemanticAnalyzer;
import semantic.SemanticError;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== PROGRAMA VÁLIDO ===");
        testValid();

        System.out.println("\n=== ERROS SEMÂNTICOS ===");
        testErrors();
    }

    static void testValid() {
        String source = """
                int x;
                int y;
                bool flag;
                x = 10;
                y = x + 5;
                flag = y > 10;
                if (flag) {
                    print(y);
                } else {
                    print(0);
                }
                """;

        ProgramNode program = buildAST(source);
        printAST(program, 0);

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(program);
        System.out.println("Análise semântica: OK");
    }

    static void testErrors() {
        // Caso 1: variável não declarada
        runWithError("Variável não declarada:", """
                int x;
                y = 5;
                """);

        // Caso 2: tipo incompatível na atribuição
        runWithError("Tipo incompatível:", """
                int x;
                bool b;
                x = true;
                """);

        // Caso 3: condição do if não é bool
        runWithError("Condição if não bool:", """
                int x;
                x = 1;
                if (x) {
                    print(x);
                }
                """);

        // Caso 4: variável declarada duas vezes
        runWithError("Declaração dupla:", """
                int x;
                int x;
                """);
    }

    static void runWithError(String label, String source) {
        try {
            ProgramNode program = buildAST(source);
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            analyzer.analyze(program);
            System.out.println(label + " ERRO — deveria ter falhado!");
        } catch (SemanticError e) {
            System.out.println(label + " OK → " + e.getMessage());
        }
    }

    static ProgramNode buildAST(String source) {
        Lexer lexer        = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        Parser parser      = new Parser(tokens);
        return parser.parse();
    }

    // Impressão da AST (mantida da sessão anterior)
    static void printAST(Node node, int indent) {
        String pad = "  ".repeat(indent);
        switch (node) {
            case ProgramNode p -> {
                System.out.println(pad + "Program");
                p.statements.forEach(s -> printAST(s, indent + 1));
            }
            case VarDeclNode v ->
                    System.out.println(pad + "VarDecl: " + v.type + " " + v.name);
            case AssignNode a -> {
                System.out.println(pad + "Assign: " + a.name + " =");
                printAST(a.expression, indent + 1);
            }
            case IfNode i -> {
                System.out.println(pad + "If");
                System.out.println(pad + "  condition:");
                printAST(i.condition, indent + 2);
                System.out.println(pad + "  then:");
                i.thenBranch.forEach(s -> printAST(s, indent + 2));
                if (!i.elseBranch.isEmpty()) {
                    System.out.println(pad + "  else:");
                    i.elseBranch.forEach(s -> printAST(s, indent + 2));
                }
            }
            case WhileNode w -> {
                System.out.println(pad + "While");
                System.out.println(pad + "  condition:");
                printAST(w.condition, indent + 2);
                System.out.println(pad + "  body:");
                w.body.forEach(s -> printAST(s, indent + 2));
            }
            case PrintNode p -> {
                System.out.println(pad + "Print");
                printAST(p.expression, indent + 1);
            }
            case BinOpNode b -> {
                System.out.println(pad + "BinOp: " + b.operator);
                printAST(b.left,  indent + 1);
                printAST(b.right, indent + 1);
            }
            case UnaryOpNode u -> {
                System.out.println(pad + "UnaryOp: " + u.operator);
                printAST(u.operand, indent + 1);
            }
            case NumberNode n     -> System.out.println(pad + "Number: " + n.value);
            case BoolNode   b     -> System.out.println(pad + "Bool: "   + b.value);
            case StringNode s     -> System.out.println(pad + "String: " + s.value);
            case IdentifierNode i -> System.out.println(pad + "Ident: "  + i.name);
            default -> System.out.println(pad + "? " + node.getClass().getSimpleName());
        }
    }
}