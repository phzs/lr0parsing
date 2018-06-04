package analysis;

import base.CFGrammar;
import base.CFProduction;
import base.Sequence;
import base.Symbol;
import parsing.ParseTable;
import parsing.ParserAction;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class Analyzer {

    public static class AnalyzerResult {
        private boolean success;
        private List<CFProduction> usedProductions;

        public AnalyzerResult(boolean success) {
            this.success = success;
        }

        public boolean getSuccess() {
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
            String result = String.valueOf(success);
            if(success && usedProductions != null && usedProductions.size() > 0) {
                result += " " + usedProductions.stream().map(Object::toString)
                        .collect(Collectors.joining(", "));
            }
            return result;
        }
    }

    public AnalyzerResult analyze(CFGrammar grammar, ParseTable parseTable, String sequenceInput) {
        AnalyzerResult result = new AnalyzerResult(true);
        Sequence sequence = new Sequence(sequenceInput);
        Stack<Character> stack = new Stack<>();

        stack.add('$');
        stack.add('0');

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

            // 3. lookup both in parseTable
            ParseTable.TableEntry tableEntry = parseTable.getEntry(stateNum, symbol);
            if(tableEntry == null) {
                System.out.println("Error: No entry found for " + stateNum + " and " + symbol + ".");
                return new AnalyzerResult(false);
            }
            ParserAction action = tableEntry.getAction();
            int newState = tableEntry.getNumber();
            System.out.println("-> found " + tableEntry + " in SAT");

            // 4. process action
            System.out.println("Processing " + action.toString().toUpperCase());
            switch (action) {
                case Shift:
                    sequence.removeFirst();
                    stack.add(symbol.getRepresentation());
                    System.out.println("\t adding to stack: " + symbol.getRepresentation());
                    stack.add(Character.forDigit(newState, 10));
                    System.out.println("\t adding to stack: " + Character.forDigit(newState, 10));
                    break;
                case Reduce:
                    if(prodNum >= 0 && prodNum < grammar.getProductionList().size()) {
                        CFProduction production = grammar.getProductionList().get(prodNum);
                        result.addProduction(production);
                        int amount = 2 * production.getRight().size();
                        for (int j = 0; j < amount; j++) stack.pop();
                        int z = Character.getNumericValue(stack.peek().charValue());
                        Symbol reduceSymbol = production.getLeft();
                        stack.add(reduceSymbol.getRepresentation());
                        System.out.println("\t adding to stack: " + reduceSymbol.getRepresentation());

                        ParseTable.TableEntry reduceEntry = parseTable.getEntry(z, reduceSymbol);
                        stack.add(Character.forDigit(reduceEntry.getNumber(), 10));
                        System.out.println("\t adding to stack: " + Character.forDigit(reduceEntry.getNumber(), 10));
                    } else {
                        System.out.println("Error: production number not in range: " + prodNum);
                        return new AnalyzerResult(false);
                    }
                    break;
                case Accept:
                    return result; // success = true
                default: //error
                    return new AnalyzerResult(false);
            }


            System.out.println(stack);
        }
        return new AnalyzerResult(false);
    }
}
