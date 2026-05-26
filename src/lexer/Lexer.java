package lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {

    private final String source;
    private int start = 0;    // início do token atual
    private int current = 0;  // posição do cursor
    private int line = 1;     // linha atual

    private final List<Token> tokens = new ArrayList<>();

    // Tabela de palavras reservadas
    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "int",   TokenType.INT,
            "bool",  TokenType.BOOL,
            "if",    TokenType.IF,
            "else",  TokenType.ELSE,
            "while", TokenType.WHILE,
            "print", TokenType.PRINT,
            "read",  TokenType.READ,
            "true",  TokenType.TRUE,
            "false", TokenType.FALSE
    );

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            // Delimitadores simples
            case '(' -> addToken(TokenType.LPAREN);
            case ')' -> addToken(TokenType.RPAREN);
            case '{' -> addToken(TokenType.LBRACE);
            case '}' -> addToken(TokenType.RBRACE);
            case ';' -> addToken(TokenType.SEMICOLON);
            case ',' -> addToken(TokenType.COMMA);

            // Operadores aritméticos
            case '+' -> addToken(TokenType.PLUS);
            case '-' -> addToken(TokenType.MINUS);
            case '*' -> addToken(TokenType.STAR);

            // Operadores que podem ser simples ou duplos
            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL  : TokenType.BANG);
            case '<' -> addToken(TokenType.LESS);
            case '>' -> addToken(TokenType.GREATER);
            case '&' -> { if (match('&')) addToken(TokenType.AND); }
            case '|' -> { if (match('|')) addToken(TokenType.OR);  }

            // Barra: divisão ou comentário de linha (//)
            case '/' -> {
                if (match('/')) {
                    // Comentário — ignora até o fim da linha
                    while (!isAtEnd() && peek() != '\n') advance();
                } else {
                    addToken(TokenType.SLASH);
                }
            }

            // Espaços e quebras de linha — ignorados
            case ' ', '\r', '\t' -> {}
            case '\n' -> line++;

            // Strings
            case '"' -> scanString();

            default -> {
                if (isDigit(c)) {
                    scanNumber();
                } else if (isAlpha(c)) {
                    scanIdentifierOrKeyword();
                } else {
                    throw new RuntimeException(
                            "Caractere inesperado '" + c + "' na linha " + line
                    );
                }
            }
        }
    }

    // -------------------------------------------------------
    // Scanners específicos
    // -------------------------------------------------------

    private void scanString() {
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            throw new RuntimeException("String não fechada na linha " + line);
        }
        advance(); // consome o '"' de fechamento
        // valor sem as aspas
        String value = source.substring(start + 1, current - 1);
        tokens.add(new Token(TokenType.STRING, value, line));
    }

    private void scanNumber() {
        while (!isAtEnd() && isDigit(peek())) advance();
        String value = source.substring(start, current);
        tokens.add(new Token(TokenType.NUMBER, value, line));
    }

    private void scanIdentifierOrKeyword() {
        while (!isAtEnd() && isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        // verifica se é palavra reservada; senão é IDENTIFIER
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        tokens.add(new Token(type, text, line));
    }

    // -------------------------------------------------------
    // Utilitários
    // -------------------------------------------------------

    private char advance() {
        return source.charAt(current++);
    }

    // Consome o próximo caractere somente se for o esperado
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    // Espia o caractere atual sem consumir
    private char peek() {
        return source.charAt(current);
    }

    private boolean isAtEnd()        { return current >= source.length(); }
    private boolean isDigit(char c)  { return c >= '0' && c <= '9'; }
    private boolean isAlpha(char c)  { return Character.isLetter(c) || c == '_'; }
    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }

    private void addToken(TokenType type) {
        String value = source.substring(start, current);
        tokens.add(new Token(type, value, line));
    }
}