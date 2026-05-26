package ast.nodes;

import ast.Node;

// int x; ou bool flag;
public class VarDeclNode implements Node {
    public final String type;        // "int" ou "bool"
    public final String name;

    public VarDeclNode(String type, String name) {
        this.type = type;
        this.name = name;
    }
}