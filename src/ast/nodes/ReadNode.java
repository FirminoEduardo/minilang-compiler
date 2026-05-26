package ast.nodes;

import ast.Node;

// read(x);
public class ReadNode implements Node {
    public final String name; // variável que recebe a leitura

    public ReadNode(String name) {
        this.name = name;
    }
}