package ast.nodes;

import ast.Node;

// referência a uma variável: x
public class IdentifierNode implements Node {
    public final String name;

    public IdentifierNode(String name) {
        this.name = name;
    }
}