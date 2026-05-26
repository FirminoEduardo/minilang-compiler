package main;

import ast.nodes.ProgramNode;
import codegen.BytecodeGenerator;
import codegen.BytecodeInstruction;
import ir.IRGenerator;
import ir.Instruction;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import semantic.SemanticAnalyzer;
import vm.VirtualMachine;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        // Programa de teste: fatorial de 5
        String source = """
                int n;
                int result;
                n = 5;
                result = 1;
                while (n > 1) {
                    result = result * n;
                    n = n - 1;
                }
                print(result);
                """;

        System.out.println("=== FONTE ===");
        System.out.println(source);

        // Fase A — Léxico
        List<Token> tokens = new Lexer(source).tokenize();

        // Fase B — Sintático
        ProgramNode program = new Parser(tokens).parse();

        // Fase C — Semântico
        new SemanticAnalyzer().analyze(program);

        // Fase D — IR/TAC
        List<Instruction> tac = new IRGenerator().generate(program);

        System.out.println("=== TAC ===");
        for (int i = 0; i < tac.size(); i++) {
            System.out.printf("%3d:  %s%n", i, tac.get(i));
        }

        // Fase E — Bytecode
        List<BytecodeInstruction> bytecode = new BytecodeGenerator().generate(tac);

        System.out.println("\n=== BYTECODE ===");
        for (int i = 0; i < bytecode.size(); i++) {
            System.out.printf("%3d:  %s%n", i, bytecode.get(i));
        }

        // Execução
        System.out.println("\n=== EXECUÇÃO ===");
        new VirtualMachine(bytecode).run();
    }
}