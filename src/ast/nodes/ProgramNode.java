package ast.nodes;

import ast.Node;
import java.util.List;

// Raiz da AST — representa o programa inteiro
public class ProgramNode implements Node {
    public final List<Node> statements;

    public ProgramNode(List<Node> statements) {
        this.statements = statements;
    }
}