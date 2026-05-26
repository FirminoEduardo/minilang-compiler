package semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    // Cada entrada guarda o tipo declarado da variável ("int" ou "bool")
    private final Map<String, String> symbols = new HashMap<>();

    // Escopo pai — permite escopos aninhados (if, while)
    private final SymbolTable parent;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    // Declara uma variável no escopo atual
    public void declare(String name, String type) {
        if (symbols.containsKey(name)) {
            throw new SemanticError("Variável já declarada neste escopo: '" + name + "'");
        }
        symbols.put(name, type);
    }

    // Busca o tipo de uma variável — sobe pelos escopos pai se necessário
    public String lookup(String name) {
        if (symbols.containsKey(name)) return symbols.get(name);
        if (parent != null)            return parent.lookup(name);
        throw new SemanticError("Variável não declarada: '" + name + "'");
    }

    // Verifica se existe (sem lançar exceção)
    public boolean contains(String name) {
        if (symbols.containsKey(name)) return true;
        if (parent != null)            return parent.contains(name);
        return false;
    }
}