package parser;

import ast.Node;
import ast.nodes.*;
import lexer.Token;
import lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // -------------------------------------------------------
    // Ponto de entrada
    // -------------------------------------------------------

    public ProgramNode parse() {
        List<Node> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(parseStatement());
        }
        return new ProgramNode(statements);
    }

    // -------------------------------------------------------
    // Statements
    // -------------------------------------------------------

    private Node parseStatement() {
        if (check(TokenType.INT) || check(TokenType.BOOL)) return parseVarDecl();
        if (check(TokenType.IF))                           return parseIf();
        if (check(TokenType.WHILE))                        return parseWhile();
        if (check(TokenType.PRINT))                        return parsePrint();
        if (check(TokenType.READ))                         return parseRead();
        if (check(TokenType.IDENTIFIER))                   return parseAssign();

        throw new RuntimeException(
                "Statement inesperado: '" + peek().value + "' na linha " + peek().line
        );
    }

    private VarDeclNode parseVarDecl() {
        Token typeToken = advance();             // consome "int" ou "bool"
        Token nameToken = expect(TokenType.IDENTIFIER, "nome de variável");
        expect(TokenType.SEMICOLON, "';'");
        return new VarDeclNode(typeToken.value, nameToken.value);
    }

    private AssignNode parseAssign() {
        Token nameToken = expect(TokenType.IDENTIFIER, "identificador");
        expect(TokenType.EQUAL, "'='");
        Node expr = parseExpression();
        expect(TokenType.SEMICOLON, "';'");
        return new AssignNode(nameToken.value, expr);
    }

    private IfNode parseIf() {
        expect(TokenType.IF, "'if'");
        expect(TokenType.LPAREN, "'('");
        Node condition = parseExpression();
        expect(TokenType.RPAREN, "')'");

        List<Node> thenBranch = parseBlock();

        List<Node> elseBranch = new ArrayList<>();
        if (check(TokenType.ELSE)) {
            advance(); // consome "else"
            elseBranch = parseBlock();
        }

        return new IfNode(condition, thenBranch, elseBranch);
    }

    private WhileNode parseWhile() {
        expect(TokenType.WHILE, "'while'");
        expect(TokenType.LPAREN, "'('");
        Node condition = parseExpression();
        expect(TokenType.RPAREN, "')'");
        List<Node> body = parseBlock();
        return new WhileNode(condition, body);
    }

    private PrintNode parsePrint() {
        expect(TokenType.PRINT, "'print'");
        expect(TokenType.LPAREN, "'('");
        Node expr = parseExpression();
        expect(TokenType.RPAREN, "')'");
        expect(TokenType.SEMICOLON, "';'");
        return new PrintNode(expr);
    }

    private ReadNode parseRead() {
        expect(TokenType.READ, "'read'");
        expect(TokenType.LPAREN, "'('");
        Token nameToken = expect(TokenType.IDENTIFIER, "identificador");
        expect(TokenType.RPAREN, "')'");
        expect(TokenType.SEMICOLON, "';'");
        return new ReadNode(nameToken.value);
    }

    private List<Node> parseBlock() {
        expect(TokenType.LBRACE, "'{'");
        List<Node> stmts = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            stmts.add(parseStatement());
        }
        expect(TokenType.RBRACE, "'}'");
        return stmts;
    }

    // -------------------------------------------------------
    // Expressões — precedência crescente de cima para baixo
    // -------------------------------------------------------

    // Nível 1: || (menor precedência)
    private Node parseExpression() {
        return parseOr();
    }

    private Node parseOr() {
        Node left = parseAnd();
        while (check(TokenType.OR)) {
            String op = advance().value;
            left = new BinOpNode(left, op, parseAnd());
        }
        return left;
    }

    // Nível 2: &&
    private Node parseAnd() {
        Node left = parseEquality();
        while (check(TokenType.AND)) {
            String op = advance().value;
            left = new BinOpNode(left, op, parseEquality());
        }
        return left;
    }

    // Nível 3: == !=
    private Node parseEquality() {
        Node left = parseComparison();
        while (check(TokenType.EQUAL_EQUAL) || check(TokenType.BANG_EQUAL)) {
            String op = advance().value;
            left = new BinOpNode(left, op, parseComparison());
        }
        return left;
    }

    // Nível 4: < >
    private Node parseComparison() {
        Node left = parseAddSub();
        while (check(TokenType.LESS) || check(TokenType.GREATER)) {
            String op = advance().value;
            left = new BinOpNode(left, op, parseAddSub());
        }
        return left;
    }

    // Nível 5: + -
    private Node parseAddSub() {
        Node left = parseMulDiv();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = advance().value;
            left = new BinOpNode(left, op, parseMulDiv());
        }
        return left;
    }

    // Nível 6: * /
    private Node parseMulDiv() {
        Node left = parseUnary();
        while (check(TokenType.STAR) || check(TokenType.SLASH)) {
            String op = advance().value;
            left = new BinOpNode(left, op, parseUnary());
        }
        return left;
    }

    // Nível 7: ! - (unário)
    private Node parseUnary() {
        if (check(TokenType.BANG) || check(TokenType.MINUS)) {
            String op = advance().value;
            return new UnaryOpNode(op, parseUnary());
        }
        return parsePrimary();
    }

    // Nível 8: literais, identificadores, (expr)
    private Node parsePrimary() {
        if (check(TokenType.NUMBER)) {
            return new NumberNode(Integer.parseInt(advance().value));
        }
        if (check(TokenType.TRUE))  { advance(); return new BoolNode(true);  }
        if (check(TokenType.FALSE)) { advance(); return new BoolNode(false); }
        if (check(TokenType.STRING)) {
            return new StringNode(advance().value);
        }
        if (check(TokenType.IDENTIFIER)) {
            return new IdentifierNode(advance().value);
        }
        if (check(TokenType.LPAREN)) {
            advance(); // consome '('
            Node expr = parseExpression();
            expect(TokenType.RPAREN, "')'");
            return expr;
        }

        throw new RuntimeException(
                "Expressão inesperada: '" + peek().value + "' na linha " + peek().line
        );
    }

    // -------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------

    private Token expect(TokenType type, String description) {
        if (check(type)) return advance();
        throw new RuntimeException(
                "Esperado " + description + ", mas encontrado '" +
                        peek().value + "' na linha " + peek().line
        );
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return tokens.get(current - 1);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return tokens.get(current).type == TokenType.EOF;
    }
}