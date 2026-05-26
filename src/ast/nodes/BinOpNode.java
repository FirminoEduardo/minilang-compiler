package ast.nodes;

import ast.Node;

// expr OP expr  →  x + y, x == y, x && y ...
public class BinOpNode implements Node {
    public final Node left;
    public final String operator; // "+", "-", "*", "/", "==", "!=", "<", ">", "&&", "||"
    public final Node right;

    public BinOpNode(Node left, String operator, Node right) {
        this.left     = left;
        this.operator = operator;
        this.right    = right;
    }
}