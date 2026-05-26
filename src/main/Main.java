package main;

import ast.Node;
import ast.nodes.*;
import ir.IRGenerator;
import ir.Instruction;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import semantic.SemanticAnalyzer;

import java.util.List;

public class Main {

    public static void main(String[] args) {
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
                int i;
                i = 1;
                while (i < 4) {
                    print(i);
                    i = i + 1;
                }
                """;

        // Fase A — Léxico
        Lexer lexer        = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        // Fase B — Sintático
        Parser parser       = new Parser(tokens);
        ProgramNode program = parser.parse();

        // Fase C — Semântico
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(program);

        // Fase D — Geração de IR
        IRGenerator irGen           = new IRGenerator();
        List<Instruction> irCode    = irGen.generate(program);

        System.out.println("=== CÓDIGO INTERMEDIÁRIO (TAC) ===");
        for (int i = 0; i < irCode.size(); i++) {
            System.out.printf("%3d:  %s%n", i, irCode.get(i));
        }
    }

    // printAST mantido para referência (pode ser removido se preferir)
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
            case NumberNode n     -> System.out.println(pad + "Number: "  + n.value);
            case BoolNode   b     -> System.out.println(pad + "Bool: "    + b.value);
            case StringNode s     -> System.out.println(pad + "String: "  + s.value);
            case IdentifierNode i -> System.out.println(pad + "Ident: "   + i.name);
            default -> System.out.println(pad + "? " + node.getClass().getSimpleName());
        }
    }
}