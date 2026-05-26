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
import semantic.SemanticError;
import vm.VirtualMachine;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     MiniLang Compiler — Testes Finais    ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        testeLexicoSintatico();
        testeSemantico();
        testeExecucao();
    }

    // -------------------------------------------------------
    // Critério 1 — Corretude Léxica/Sintática (30%)
    // -------------------------------------------------------

    static void testeLexicoSintatico() {
        printHeader("CRITÉRIO 1 — LÉXICO E SINTÁTICO");

        // Programa válido complexo
        runTest("Programa válido (aceita)", """
                int x;
                bool flag;
                x = 42;
                flag = x > 10;
                if (flag) {
                    print(x);
                } else {
                    print(0);
                }
                """, true);

        // Símbolo inválido
        runTest("Símbolo inválido '@' (rejeita)", """
                int x;
                x = @5;
                """, false);

        // Falta ponto-e-vírgula
        runTest("Falta ';' (rejeita)", """
                int x
                x = 5;
                """, false);

        // Expressão incompleta
        runTest("Expressão incompleta 'x = 5 +' (rejeita)", """
                int x;
                x = 5 +;
                """, false);

        // Bloco não fechado
        runTest("Bloco não fechado (rejeita)", """
                int x;
                x = 1;
                while (x > 0) {
                    print(x);
                """, false);
    }

    // -------------------------------------------------------
    // Critério 2 — Análise Semântica (20%)
    // -------------------------------------------------------

    static void testeSemantico() {
        printHeader("CRITÉRIO 2 — ANÁLISE SEMÂNTICA");

        runTest("Variável não declarada (rejeita)", """
                x = 5;
                """, false);

        runTest("Declaração duplicada (rejeita)", """
                int x;
                int x;
                """, false);

        runTest("Tipo incompatível: int recebe bool (rejeita)", """
                int x;
                x = true;
                """, false);

        runTest("Tipo incompatível: bool recebe int (rejeita)", """
                bool b;
                b = 42;
                """, false);

        runTest("Condição if com int (rejeita)", """
                int x;
                x = 1;
                if (x) { print(x); }
                """, false);

        runTest("Aritmética com bool (rejeita)", """
                bool a;
                bool b;
                int r;
                a = true;
                b = false;
                r = a + b;
                """, false);

        runTest("Programa semanticamente válido (aceita)", """
                int a;
                int b;
                bool ok;
                a = 3;
                b = 7;
                ok = a < b;
                if (ok) {
                    print(b);
                }
                """, true);
    }

    // -------------------------------------------------------
    // Critério 3 — Geração de Código / Execução (30%)
    // -------------------------------------------------------

    static void testeExecucao() {
        printHeader("CRITÉRIO 3 — EXECUÇÃO");

        // Fatorial de 5 → 120
        runAndExecute("Fatorial de 5 (esperado: 120)", """
                int n;
                int result;
                n = 5;
                result = 1;
                while (n > 1) {
                    result = result * n;
                    n = n - 1;
                }
                print(result);
                """);

        // Soma de 1 a 5 → 15
        runAndExecute("Soma 1 a 5 (esperado: 15)", """
                int i;
                int soma;
                i = 1;
                soma = 0;
                while (i < 6) {
                    soma = soma + i;
                    i = i + 1;
                }
                print(soma);
                """);

        // If/else com operador lógico
        runAndExecute("If/else aninhado (esperado: 1)", """
                int x;
                int y;
                bool cond;
                x = 10;
                y = 20;
                cond = x < y;
                if (cond) {
                    print(1);
                } else {
                    print(0);
                }
                """);

        // Expressão com múltiplos operadores
        runAndExecute("Expressão composta — precedência * antes de + (esperado: 14)", """
                int a;
                int b;
                int c;
                a = 2;
                b = 3;
                c = a + b * 4;
                print(c);
                """);
    }

    // -------------------------------------------------------
    // Utilitários de teste
    // -------------------------------------------------------

    static void runTest(String label, String source, boolean expectSuccess) {
        try {
            ProgramNode program = buildAST(source);
            new SemanticAnalyzer().analyze(program);
            if (expectSuccess) {
                System.out.println("  ✓ " + label);
            } else {
                System.out.println("  ✗ " + label + " — ERRO: deveria ter falhado");
            }
        } catch (Exception e) {
            if (!expectSuccess) {
                System.out.println("  ✓ " + label + " → " + e.getMessage());
            } else {
                System.out.println("  ✗ " + label + " — ERRO inesperado: " + e.getMessage());
            }
        }
    }

    static void runAndExecute(String label, String source) {
        System.out.println("\n  → " + label);
        try {
            ProgramNode program              = buildAST(source);
            new SemanticAnalyzer().analyze(program);
            List<Instruction> tac            = new IRGenerator().generate(program);
            List<BytecodeInstruction> bc     = new BytecodeGenerator().generate(tac);
            System.out.print("    Saída: ");
            new VirtualMachine(bc).run();
        } catch (Exception e) {
            System.out.println("    ERRO: " + e.getMessage());
        }
    }

    static ProgramNode buildAST(String source) {
        List<Token> tokens = new Lexer(source).tokenize();
        return new Parser(tokens).parse();
    }

    static void printHeader(String title) {
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.printf( "│  %-39s│%n", title);
        System.out.println("└─────────────────────────────────────────┘");
    }
}