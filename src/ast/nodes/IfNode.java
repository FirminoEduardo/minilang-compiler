package ast.nodes;

import ast.Node;
import java.util.List;

// if (condição) { ... } else { ... }
public class IfNode implements Node {
    public final Node condition;
    public final List<Node> thenBranch;
    public final List<Node> elseBranch; // pode ser lista vazia

    public IfNode(Node condition, List<Node> thenBranch, List<Node> elseBranch) {
        this.condition  = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
}