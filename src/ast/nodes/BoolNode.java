package ast.nodes;

import ast.Node;

// literal booleano: true / false
public class BoolNode implements Node {
    public final boolean value;

    public BoolNode(boolean value) {
        this.value = value;
    }
}