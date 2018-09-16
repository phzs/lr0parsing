package analysis;

import base.CFGrammar;
import base.CFProduction;
import base.Sequence;
import base.Symbol;
import parsing.ParseTable;
import parsing.ParserAction;
import visualization.StepController;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Analyzer {
    private ObservableStack<Character> stack;

    private AnalyzerResult result;

    public Analyzer() {
        this.stack = new ObservableStack<>();
        this.result = new AnalyzerResult();
    }

    public ObservableStack<Character> getStack() {
        return this.stack;
    }


    public Void analyze(CFGrammar grammar, ParseTable parseTable, String sequenceInput) {
        this.stack.clear();

        Sequence sequence = new Sequence(sequenceInput);

        stack.push('$');
        stack.push('0');

        int size = sequence.size();
        for(int i = 0; i < size; i++) {
            // 1. take top element of stack
            Character top = stack.peek(); //pop?
            if(!Character.isDigit(top))
                throw new InternalError("Expected digit on top of the stack.");
            int stateNum = Character.getNumericValue(top.charValue());
            int prodNum = stateNum;

            // 2. read next symbol of input
            Symbol symbol = sequence.get(0);

            StepController.getInstance().registerStep("analyze:LookupParseTable", "Popped from stack, looking up in parse table");

            // 3. lookup both in parseTable
            ParseTable.TableEntry tableEntry = parseTable.getEntry(stateNum, symbol);
            if(tableEntry == null) {
                System.out.println("Error: No entry found for " + stateNum + " and " + symbol + ".");
                return null;
            }
            ParserAction action = tableEntry.getAction();
            int newState = tableEntry.getNumber();
            System.out.println("-> found " + tableEntry + " in SAT");

            // 4. process action
            System.out.println("Processing " + action.toString().toUpperCase());
            switch (action) {
                case Shift:
                    sequence.removeFirst();
                    stack.push(symbol.getRepresentation());
                    System.out.println("\t adding to stack: " + symbol.getRepresentation());
                    stack.push(Character.forDigit(newState, 10));
                    System.out.println("\t adding to stack: " + Character.forDigit(newState, 10));
                    StepController.getInstance().registerStep("analyze:ActionShift", "Shift action");
                    break;
                case Reduce:
                    if(prodNum >= 0 && prodNum < grammar.getProductionList().size()) {
                        CFProduction production = grammar.getProductionList().get(prodNum);
                        result.addProduction(production);
                        int amount = 2 * production.getRight().size();
                        for (int j = 0; j < amount; j++) stack.pop();
                        int z = Character.getNumericValue(stack.peek().charValue());
                        Symbol reduceSymbol = production.getLeft();
                        stack.push(reduceSymbol.getRepresentation());
                        System.out.println("\t adding to stack: " + reduceSymbol.getRepresentation());

                        ParseTable.TableEntry reduceEntry = parseTable.getEntry(z, reduceSymbol);
                        stack.push(Character.forDigit(reduceEntry.getNumber(), 10));
                        System.out.println("\t adding to stack: " + Character.forDigit(reduceEntry.getNumber(), 10));
                        StepController.getInstance().registerStep("analyze:ActionReduce", "Reduce action");
                    } else {
                        System.out.println("Error: production number not in range: " + prodNum);
                        result.setSuccess(false);
                        return null;
                    }
                    break;
                case Accept:
                    result.setSuccess(true);
                    return null;
                default: //error
                    result.setSuccess(false);
                    return null;
            }

            System.out.println(stack);
        }
        return null;
    }

    public AnalyzerResult getResult() {
        return result;
    }

    public static class AnalyzerResult {
        private Boolean success;
        private List<CFProduction> usedProductions;

        public AnalyzerResult() {
            this(null);
        }

        public AnalyzerResult(Boolean success) {
            this.success = success;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public List<CFProduction> getUsedProductions() {
            return usedProductions;
        }

        public void setUsedProductions(List<CFProduction> usedProductions) {
            this.usedProductions = usedProductions;
        }

        public void addProduction(CFProduction production) {
            if(usedProductions == null)
                usedProductions = new LinkedList<>();
            usedProductions.add(production);
        }

        @Override
        public String toString() {
            String result = "";
            if(success == null)
                result = "Unknown";
            else if(success && usedProductions != null && usedProductions.size() > 0) {
                result += String.valueOf(success) + " " + usedProductions.stream().map(Object::toString)
                        .collect(Collectors.joining(", "));
            }
            return result;
        }
    }
}
