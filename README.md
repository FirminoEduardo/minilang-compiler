# MiniLang Compiler

Compilador completo para a linguagem **MiniLang**, desenvolvido como projeto
da disciplina de Compiladores. Implementa todas as fases clássicas de tradução,
desde a análise léxica até a execução em uma máquina virtual baseada em pilha.

---

## Arquitetura

```
Código-fonte (.ml)
      │
      ▼
┌─────────────┐
│  A — Lexer  │  Transforma caracteres em tokens
└──────┬──────┘
       ▼
┌─────────────┐
│  B — Parser │  Constrói a Árvore de Sintaxe Abstrata (AST)
└──────┬──────┘
       ▼
┌──────────────────┐
│  C — Semântica   │  Verifica tipos e declarações
└──────┬───────────┘
       ▼
┌─────────────────────┐
│  D — IR (TAC)       │  Gera código de três endereços
└──────┬──────────────┘
       ▼
┌──────────────────────────┐
│  E — Bytecode + VM       │  Executa em máquina virtual de pilha
└──────────────────────────┘
```

---

## A Linguagem MiniLang

### Tipos
| Tipo   | Exemplo            |
|--------|--------------------|
| `int`  | `42`, `-7`         |
| `bool` | `true`, `false`    |

### Estruturas suportadas

```
// Declaração de variável
int x;
bool flag;

// Atribuição
x = 10;
flag = x > 5;

// Condicional
if (flag) {
    print(x);
} else {
    print(0);
}

// Laço
while (x > 0) {
    print(x);
    x = x - 1;
}

// Entrada e saída
read(x);
print(x);
```

### Operadores
| Categoria    | Operadores          |
|--------------|---------------------|
| Aritméticos  | `+`  `-`  `*`  `/`  |
| Relacionais  | `<`  `>`  `==`  `!=`|
| Lógicos      | `&&` `\|\|` `!`     |
| Atribuição   | `=`                 |

---

## Estrutura do Projeto

```
minilang-compiler/
└── src/
    ├── lexer/
    │   ├── TokenType.java       # Enum com todos os tipos de token
    │   ├── Token.java           # Representa um token (tipo, valor, linha)
    │   └── Lexer.java           # Scanner baseado em AFD manual
    ├── ast/
    │   ├── Node.java            # Interface base dos nós da AST
    │   └── nodes/               # Um arquivo por tipo de nó
    ├── parser/
    │   └── Parser.java          # Parser descendente recursivo
    ├── semantic/
    │   ├── SemanticError.java   # Exceção customizada
    │   ├── SymbolTable.java     # Tabela de símbolos com escopos aninhados
    │   └── SemanticAnalyzer.java# Verificador de tipos e declarações
    ├── ir/
    │   ├── OpCode.java          # Enum das operações TAC
    │   ├── Instruction.java     # Uma instrução de três endereços
    │   └── IRGenerator.java     # Gerador de código intermediário
    ├── codegen/
    │   ├── BytecodeOp.java      # Enum das operações da VM
    │   ├── BytecodeInstruction.java
    │   └── BytecodeGenerator.java # Traduz TAC → bytecode
    ├── vm/
    │   └── VirtualMachine.java  # Executa o bytecode (VM de pilha)
    └── main/
        └── Main.java            # Pipeline completo + suíte de testes
```

---

## Como Executar

**Requisitos:** Java 23+ (OpenJDK 23.0.2), IntelliJ IDEA

1. Clone o repositório
2. Abra no IntelliJ IDEA como projeto Java
3. Execute `main/Main.java`

---

## Fases do Compilador

### A — Análise Léxica
O `Lexer` percorre o código-fonte caractere a caractere, aplicando
um AFD manual para reconhecer tokens. Ignora espaços em branco e
comentários de linha (`//`).

### B — Análise Sintática
O `Parser` implementa um **parser descendente recursivo** com uma
função por regra gramatical. Cada nível de função corresponde a um
nível de precedência de operadores, do mais fraco (`||`) ao mais
forte (primários).

### C — Análise Semântica
O `SemanticAnalyzer` percorre a AST e mantém uma `SymbolTable`
hierárquica que sobe pelos escopos pai para resolução de nomes.
Erros detectados: variável não declarada, declaração duplicada,
incompatibilidade de tipos e condições não-booleanas.

### D — Código Intermediário (TAC)
O `IRGenerator` converte a AST em **código de três endereços**,
introduzindo temporários (`t0`, `t1`, ...) e labels (`L0`, `L1`, ...).
Essa representação é independente de máquina e facilita otimização.

### E — Bytecode e Máquina Virtual
O `BytecodeGenerator` traduz as instruções TAC para bytecode de uma
**VM baseada em pilha**. A `VirtualMachine` executa esse bytecode
mantendo uma pilha de operandos e uma memória de variáveis.
Labels são pré-indexadas para que saltos sejam realizados em O(1).

---

## Exemplo Completo — Fatorial de 5

**Entrada:**
```
int n;
int result;
n = 5;
result = 1;
while (n > 1) {
    result = result * n;
    n = n - 1;
}
print(result);
```

**TAC gerado:**
```
 0:  n = 5
 1:  result = 1
 2:  LABEL L0
 3:  t0 = n > 1
 4:  IF_FALSE t0 GOTO L1
 5:  t1 = result * n
 6:  result = t1
 7:  t2 = n - 1
 8:  n = t2
 9:  GOTO L0
10:  LABEL L1
11:  PRINT result
```

**Saída:** `120`