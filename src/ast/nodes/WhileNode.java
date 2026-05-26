package ast.nodes;

import ast.Node;
import java.util.List;

// while (condição) { ... }
public class WhileNode implements Node {
    public final Node condition;
    public final List<Node> body;

    public WhileNode(Node condition, List<Node> body) {
        this.condition = condition;
        this.body      = body;
    }
}