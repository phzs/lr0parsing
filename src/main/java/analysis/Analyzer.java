package analysis;

import base.CFGrammar;
import base.CFProduction;
import base.Sequence;
import base.Symbol;
import parsing.ParserAction;
import parsing.ParseTable;

import java.util.Stack;

public class Analyzer {

    public boolean analyze(CFGrammar grammar, ParseTable parseTable, String sequenceInput) {
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
                return false;
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
                        return false;
                    }
                    break;
                case Accept:
                    return true;
                default: //error
                    return false;
            }


            System.out.println(stack);
        }

        return true;
    }
}
