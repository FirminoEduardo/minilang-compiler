package ast.nodes;

import ast.Node;

// literal inteiro: 42
public class NumberNode implements Node {
    public final int value;

    public NumberNode(int value) {
        this.value = value;
    }
}