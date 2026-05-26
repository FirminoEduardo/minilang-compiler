package main;

// Classe de orquestração

import lexer.Lexer;
import lexer.Token;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String source = """
                int x;
                int y;
                x = 10;
                y = x + 5;
                if (y > 10) {
                    print(y);
                }
                """;

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}