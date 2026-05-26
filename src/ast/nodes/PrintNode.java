package ast.nodes;

import ast.Node;

// print(expressão);
public class PrintNode implements Node {
    public final Node expression;

    public PrintNode(Node expression) {
        this.expression = expression;
    }
}