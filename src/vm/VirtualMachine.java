package vm;

import codegen.BytecodeInstruction;
import codegen.BytecodeOp;

import java.util.*;

public class VirtualMachine {

    private final List<BytecodeInstruction> bytecode;
    private final Map<String, Object>       memory     = new HashMap<>();
    private final Deque<Object>             stack      = new ArrayDeque<>();
    private final Map<String, Integer>      labelIndex = new HashMap<>();
    private int pc = 0;

    private final Scanner scanner;

    // Construtor padrão — cria Scanner próprio (usado pela bateria de testes)
    public VirtualMachine(List<BytecodeInstruction> bytecode) {
        this.bytecode = bytecode;
        this.scanner  = new Scanner(System.in);
        buildLabelIndex();
    }

    // Construtor com Scanner externo — evita conflito quando Main também lê stdin
    public VirtualMachine(List<BytecodeInstruction> bytecode, Scanner scanner) {
        this.bytecode = bytecode;
        this.scanner  = scanner;
        buildLabelIndex();
    }

    // Pré-computa posições dos labels para jumps em O(1)
    private void buildLabelIndex() {
        for (int i = 0; i < bytecode.size(); i++) {
            BytecodeInstruction instr = bytecode.get(i);
            if (instr.op == BytecodeOp.LABEL) {
                labelIndex.put(instr.operand, i);
            }
        }
    }

    public void run() {
        while (pc < bytecode.size()) {
            BytecodeInstruction instr = bytecode.get(pc++);

            switch (instr.op) {

                case PUSH  -> stack.push(parseLiteral(instr.operand));

                case LOAD  -> {
                    if (!memory.containsKey(instr.operand)) {
                        throw new RuntimeException(
                                "Variável não inicializada: '" + instr.operand + "'"
                        );
                    }
                    stack.push(memory.get(instr.operand));
                }

                case STORE -> memory.put(instr.operand, stack.pop());

                case ADD   -> { int b = popInt(), a = popInt(); stack.push(a + b); }
                case SUB   -> { int b = popInt(), a = popInt(); stack.push(a - b); }
                case MUL   -> { int b = popInt(), a = popInt(); stack.push(a * b); }
                case DIV   -> {
                    int b = popInt(), a = popInt();
                    if (b == 0) throw new RuntimeException("Divisão por zero");
                    stack.push(a / b);
                }

                case EQ    -> { Object b = stack.pop(), a = stack.pop(); stack.push(a.equals(b));  }
                case NEQ   -> { Object b = stack.pop(), a = stack.pop(); stack.push(!a.equals(b)); }
                case LT    -> { int b = popInt(), a = popInt(); stack.push(a < b);  }
                case GT    -> { int b = popInt(), a = popInt(); stack.push(a > b);  }
                case AND   -> { boolean b = popBool(), a = popBool(); stack.push(a && b); }
                case OR    -> { boolean b = popBool(), a = popBool(); stack.push(a || b); }
                case NEG   -> stack.push(-popInt());
                case NOT   -> stack.push(!popBool());

                case LABEL -> { /* no-op — só marca posição */ }

                case JUMP  -> pc = labelIndex.get(instr.operand) + 1;

                case JUMP_IF_FALSE -> {
                    if (!popBool()) pc = labelIndex.get(instr.operand) + 1;
                }

                case PRINT -> System.out.println(stack.pop());

                case READ  -> {
                    System.out.print(">>> leia " + instr.operand + ": ");
                    String line = scanner.nextLine().trim();
                    try {
                        memory.put(instr.operand, Integer.parseInt(line));
                    } catch (NumberFormatException e) {
                        if (line.equals("true") || line.equals("false")) {
                            memory.put(instr.operand, Boolean.parseBoolean(line));
                        } else {
                            memory.put(instr.operand, line);
                        }
                    }
                }

                case HALT -> { return; }
            }
        }
    }

    private int     popInt()  { return (int)     stack.pop(); }
    private boolean popBool() { return (boolean) stack.pop(); }

    private Object parseLiteral(String value) {
        if (value.equals("true"))   return true;
        if (value.equals("false"))  return false;
        if (value.startsWith("\"")) return value.substring(1, value.length() - 1);
        return Integer.parseInt(value);
    }
}