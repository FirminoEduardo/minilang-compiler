package ast.nodes;

import ast.Node;

// x = expressão;
public class AssignNode implements Node {
    public final String name;
    public final Node expression;

    public AssignNode(String name, Node expression) {
        this.name = name;
        this.expression = expression;
    }
}