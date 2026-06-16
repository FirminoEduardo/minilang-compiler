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
import java.util.Scanner;

public class Main {

    // Scanner compartilhado entre Main e VirtualMachine
    static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) {
        printBanner();
        boolean rodando = true;
        while (rodando) {
            printMenu();
            String opcao = input.nextLine().trim();
            switch (opcao) {
                case "1" -> modoInterativo();
                case "2" -> modoBateriaCompleta();
                case "0" -> { rodando = false; System.out.println("\n  Encerrando.\n"); }
                default  -> System.out.println("\n  Opção inválida. Tente novamente.\n");
            }
        }
    }

    // -------------------------------------------------------
    // Modo interativo — professora digita o programa
    // -------------------------------------------------------

    static void modoInterativo() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║         MODO INTERATIVO                  ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("  Digite o programa MiniLang linha a linha.");
        System.out.println("  Finalize com uma linha em branco.\n");

        StringBuilder sb = new StringBuilder();
        while (input.hasNextLine()) {
            String linha = input.nextLine();
            if (linha.isEmpty()) break;
            sb.append(linha).append("\n");
        }

        String source = sb.toString().trim();
        if (source.isEmpty()) {
            System.out.println("\n  Nenhum programa digitado.\n");
            return;
        }

        System.out.println("\n  ─────────────────────────────────────────");
        compilarEExecutar(source);
        System.out.println("  ─────────────────────────────────────────\n");
    }

    // -------------------------------------------------------
    // Compilar e executar com relatório fase a fase
    // -------------------------------------------------------

    static void compilarEExecutar(String source) {
        // Fase 1 — Léxico
        List<Token> tokens;
        try {
            tokens = new Lexer(source).tokenize();
            System.out.println("  ✓ Análise léxica    — OK");
        } catch (Exception e) {
            System.out.println("  ✗ Análise léxica    — ERRO: " + e.getMessage());
            imprimirResultado(false);
            return;
        }

        // Fase 2 — Sintático
        ProgramNode program;
        try {
            program = new Parser(tokens).parse();
            System.out.println("  ✓ Análise sintática — OK");
        } catch (Exception e) {
            System.out.println("  ✗ Análise sintática — ERRO: " + e.getMessage());
            imprimirResultado(false);
            return;
        }

        // Fase 3 — Semântico
        try {
            new SemanticAnalyzer().analyze(program);
            System.out.println("  ✓ Análise semântica — OK");
        } catch (Exception e) {
            System.out.println("  ✗ Análise semântica — ERRO: " + e.getMessage());
            imprimirResultado(false);
            return;
        }

        // Fase 4 + 5 — IR e Bytecode
        List<BytecodeInstruction> bc;
        try {
            List<Instruction> tac = new IRGenerator().generate(program);
            bc = new BytecodeGenerator().generate(tac);
            System.out.println("  ✓ Geração de código — OK");
        } catch (Exception e) {
            System.out.println("  ✗ Geração de código — ERRO: " + e.getMessage());
            imprimirResultado(false);
            return;
        }

        // Fase 6 — Execução
        imprimirResultado(true);
        System.out.println("  ── Saída da execução ──\n");
        try {
            new VirtualMachine(bc, input).run();
        } catch (Exception e) {
            System.out.println("\n  ✗ Erro em tempo de execução: " + e.getMessage());
        }
        System.out.println();
    }

    static void imprimirResultado(boolean aceito) {
        System.out.println();
        if (aceito) {
            System.out.println("  ╔════════════════╗");
            System.out.println("  ║  ✓  ACEITO     ║");
            System.out.println("  ╚════════════════╝");
        } else {
            System.out.println("  ╔════════════════╗");
            System.out.println("  ║  ✗  REJEITADO  ║");
            System.out.println("  ╚════════════════╝");
        }
        System.out.println();
    }

    // -------------------------------------------------------
    // Modo bateria — roda os 12 testes automaticamente
    // -------------------------------------------------------

    static void modoBateriaCompleta() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║     MiniLang Compiler — Testes Finais    ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
        testeLexicoSintatico();
        testeSemantico();
        testeExecucao();
        System.out.println();
    }

    // -------------------------------------------------------
    // Critério 1 — Corretude Léxica/Sintática (30%)
    // -------------------------------------------------------

    static void testeLexicoSintatico() {
        printHeader("CRITÉRIO 1 — LÉXICO E SINTÁTICO");

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

        runTest("Símbolo inválido '@' (rejeita)", """
                int x;
                x = @5;
                """, false);

        runTest("Falta ';' (rejeita)", """
                int x
                x = 5;
                """, false);

        runTest("Expressão incompleta 'x = 5 +' (rejeita)", """
                int x;
                x = 5 +;
                """, false);

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
            ProgramNode program          = buildAST(source);
            new SemanticAnalyzer().analyze(program);
            List<Instruction> tac        = new IRGenerator().generate(program);
            List<BytecodeInstruction> bc = new BytecodeGenerator().generate(tac);
            System.out.print("    Saída: ");
            new VirtualMachine(bc, input).run();
        } catch (Exception e) {
            System.out.println("    ERRO: " + e.getMessage());
        }
    }

    static ProgramNode buildAST(String source) {
        List<Token> tokens = new Lexer(source).tokenize();
        return new Parser(tokens).parse();
    }

    // -------------------------------------------------------
    // Banner e menu
    // -------------------------------------------------------

    static void printBanner() {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║        COMPILADOR MINILANG               ║");
        System.out.println("║      Projeto de Compiladores             ║");
        System.out.println("╚══════════════════════════════════════════╝\n");
    }

    static void printMenu() {
        System.out.println("  ┌──────────────────────────────────────┐");
        System.out.println("  │  1. Digitar programa                 │");
        System.out.println("  │  2. Executar bateria de testes       │");
        System.out.println("  │  0. Sair                             │");
        System.out.println("  └──────────────────────────────────────┘");
        System.out.print("  Opção: ");
    }

    static void printHeader(String title) {
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.printf( "│  %-39s│%n", title);
        System.out.println("└─────────────────────────────────────────┘");
    }
}