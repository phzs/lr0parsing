package analysis;

import base.CFGrammarListener;
import base.Symbol;
import parsing.ParseTable;

public interface AnalyzerListener {
    void onChanged(Change change);

    enum ChangeType {
        Lookup,
        Shift,
        Reduce,
        Reduce2,
        ReduceLookup,
        ReduceFinal,
        Accept,
        Error
    }

    class Change {
        private AnalyzerListener.ChangeType type;
        private Symbol markedSymbol;
        private int markedStateNum;
        private int markedProduction;
        private ParseTable.TableEntry lookupResult;
        private int reducePopAmount;

        public AnalyzerListener.ChangeType getType() {
            return type;
        }

        public void setType(AnalyzerListener.ChangeType type) {
            this.type = type;
        }

        public Symbol getMarkedSymbol() {
            return markedSymbol;
        }

        public void setMarkedSymbol(Symbol markedSymbol) {
            this.markedSymbol = markedSymbol;
        }

        public int getMarkedStateNum() {
            return markedStateNum;
        }

        public void setMarkedStateNum(int markedStateNum) {
            this.markedStateNum = markedStateNum;
        }

        public int getMarkedProduction() {
            return markedProduction;
        }

        public void setMarkedProduction(int markedProduction) {
            this.markedProduction = markedProduction;
        }

        public ParseTable.TableEntry getLookupResult() {
            return lookupResult;
        }

        public void setLookupResult(ParseTable.TableEntry lookupResult) {
            this.lookupResult = lookupResult;
        }

        public int getReducePopAmount() {
            return reducePopAmount;
        }

        public void setReducePopAmount(int reducePopAmount) {
            this.reducePopAmount = reducePopAmount;
        }
    }
}
