package ast.nodes;

import ast.Node;

// !expr ou -expr
public class UnaryOpNode implements Node {
    public final String operator; // "!" ou "-"
    public final Node operand;

    public UnaryOpNode(String operator, Node operand) {
        this.operator = operator;
        this.operand  = operand;
    }
}