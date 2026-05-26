package ast.nodes;

import ast.Node;

// literal string: "hello"
public class StringNode implements Node {
    public final String value;

    public StringNode(String value) {
        this.value = value;
    }
}