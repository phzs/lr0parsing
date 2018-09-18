package analysis;

import base.*;
import parsing.ParseTable;
import parsing.ParserAction;
import visualization.StepController;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Analyzer {
    private ObservableStack<Character> stack;

    private AnalyzerResult result;

    private List<AnalyzerListener> listeners;

    public Analyzer() {
        this.stack = new ObservableStack<>();
        this.result = new AnalyzerResult();
        this.listeners = new LinkedList<>();
    }

    public ObservableStack<Character> getStack() {
        return this.stack;
    }


    public Void analyze(CFGrammar grammar, ParseTable parseTable, String sequenceInput) {
        this.stack.clear();
        this.result = new AnalyzerResult();

        Sequence sequence = new Sequence(sequenceInput);

        stack.push('$');
        stack.push('0');

        int size = sequence.size();
        while(result.getSuccess() == null) {
            // 1. take top element of stack
            Character top = stack.peek(); //pop?
            if(!Character.isDigit(top))
                throw new InternalError("Expected digit on top of the stack.");
            int stateNum = Character.getNumericValue(top.charValue());

            // 2. read next symbol of input
            Symbol symbol = sequence.get(0);


            // 3. lookup both in parseTable
            ParseTable.TableEntry tableEntry = parseTable.getEntry(stateNum, symbol);
            if(tableEntry == null) {
                System.out.println("Error: No entry found for " + stateNum + " and " + symbol + ".");
                result.setSuccess(false);
                changeForError(stateNum, symbol);
                StepController.getInstance().registerStep("Analyze:Final", "Analysis finished");
                return null;
            }
            changeForLookup(symbol, stateNum, tableEntry);
            StepController.getInstance().registerStep("analyze:LookupParseTable", "Looking up in parse table");
            ParserAction action = tableEntry.getAction();
            System.out.println("-> found " + tableEntry + " in SAT");

            // 4. process action
            System.out.println("Processing " + action.toString().toUpperCase());
            switch (action) {
                case Shift:
                    int newState = tableEntry.getNumber();
                    sequence.removeFirst();
                    stack.push(symbol.getRepresentation());
                    System.out.println("\t adding to stack: " + symbol.getRepresentation());
                    stack.push(Character.forDigit(newState, 10));
                    System.out.println("\t adding to stack: " + Character.forDigit(newState, 10));
                    changeForShift(symbol, newState);
                    StepController.getInstance().registerStep("analyze:ActionShift", "Shift action");
                    break;
                case Reduce:
                    int prodNum = tableEntry.getNumber();
                    if(prodNum >= 0 && prodNum < grammar.getProductionList().size()) {
                        CFProduction production = grammar.getProductionList().get(prodNum);
                        result.addProduction(production);
                        int amount = 2 * production.getRight().size();

                        changeForReduce(prodNum, amount, sequence);
                        StepController.getInstance().registerStep("analyze:ActionReduce1", "Ready to delete the top "+amount+" elements from stack.");

                        for (int j = 0; j < amount; j++) stack.pop();
                        int z = Character.getNumericValue(stack.peek().charValue());
                        Symbol reduceSymbol = production.getLeft();
                        stack.push(reduceSymbol.getRepresentation());
                        System.out.println("\t adding to stack: " + reduceSymbol.getRepresentation());

                        changeForReduce2(prodNum, reduceSymbol);
                        StepController.getInstance().registerStep("analyze:ActionReduce2", "Pushed meta symbol to the stack and looking up table entry for the last two stack entries.");

                        ParseTable.TableEntry reduceEntry = parseTable.getEntry(z, reduceSymbol);
                        changeForReduceLookup(z, reduceSymbol, reduceEntry);
                        StepController.getInstance().registerStep("analyze:ActionReduce3", "Looked up parse table entry matching the last two stack entries.");

                        stack.push(Character.forDigit(reduceEntry.getNumber(), 10));
                        System.out.println("\t adding to stack: " + Character.forDigit(reduceEntry.getNumber(), 10));

                        changeForReduceFinal();
                        StepController.getInstance().registerStep("analyze:ActionReduce", "Reduce action");

                    } else {
                        System.out.println("Error: production number not in range: " + prodNum);
                        result.setSuccess(false);
                        changeForError(stateNum, symbol);
                        StepController.getInstance().registerStep("Analyze:Final", "Analysis finished");
                        return null;
                    }
                    break;
                case Accept:
                    result.setSuccess(true);
                    changeForAccept(stateNum, symbol);
                    StepController.getInstance().registerStep("Analyze:Final", "Analysis finished");
                    return null;
                default: //error
                    result.setSuccess(false);
                    changeForError(stateNum, symbol);
                    StepController.getInstance().registerStep("Analyze:Final", "Analysis finished");
                    return null;
            }
            System.out.println(stack);
        }
        StepController.getInstance().registerStep("Analyze:Final", "Analysis finished");
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

    private void propagateChange(AnalyzerListener.Change change) {
        for(AnalyzerListener listener : listeners)
            listener.onChanged(change);
    }

    private void changeForShift(Symbol symbol, int newState) {
        AnalyzerListener.Change change = new AnalyzerListener.Change();
        change.setMarkedSymbol(symbol);
        change.setMarkedStateNum(newState);
        change.setType(AnalyzerListener.ChangeType.Shift);
        propagateChange(change);
    }

    private void changeForLookup(Symbol symbol, int newState, ParseTable.TableEntry lookupResult) {
        AnalyzerListener.Change change = new AnalyzerListener.Change();
        change.setMarkedSymbol(symbol);
        change.setMarkedStateNum(newState);
        change.setType(AnalyzerListener.ChangeType.Lookup);
        change.setLookupResult(lookupResult);
        propagateChange(change);
    }

    private void changeForReduce(int prodNum, int amount, Sequence sequence) {
        AnalyzerListener.Change change = new AnalyzerListener.Change();
        change.setMarkedProduction(prodNum);
        change.setReducePopAmount(amount);
        change.setType(AnalyzerListener.ChangeType.Reduce);
        change.setSequence(sequence);
        propagateChange(change);
    }

    private void changeForReduce2(int prodNum, Symbol metaSymbol) {
        AnalyzerListener.Change change = new AnalyzerListener.Change();
        change.setMarkedProduction(prodNum);
        change.setMarkedSymbol(metaSymbol);
        change.setType(AnalyzerListener.ChangeType.Reduce2);
        propagateChange(change);
    }

    private void changeForReduceLookup(int stateNum, Symbol metaSymbol, ParseTable.TableEntry lookupResult) {
        AnalyzerListener.Change change = new AnalyzerListener.Change();
        change.setMarkedStateNum(stateNum);
        change.setMarkedSymbol(metaSymbol);
        change.setType(AnalyzerListener.ChangeType.ReduceLookup);
        change.setLookupResult(lookupResult);
        propagateChange(change);
    }

    private void changeForReduceFinal() {
        AnalyzerListener.Change change = new AnalyzerListener.Change();
        change.setType(AnalyzerListener.ChangeType.ReduceFinal);
        propagateChange(change);
    }

    private void changeForAccept(int stateNum, Symbol symbol) {
        AnalyzerListener.Change change = new AnalyzerListener.Change();
        change.setMarkedStateNum(stateNum);
        change.setMarkedSymbol(symbol);
        change.setType(AnalyzerListener.ChangeType.Accept);
        propagateChange(change);
    }

    private void changeForError(int stateNum, Symbol symbol) {
        AnalyzerListener.Change change = new AnalyzerListener.Change();
        change.setMarkedStateNum(stateNum);
        change.setMarkedSymbol(symbol);
        change.setType(AnalyzerListener.ChangeType.Error);
        propagateChange(change);
    }

    public void addListener(AnalyzerListener listener) {
        listeners.add(listener);
    }

}
