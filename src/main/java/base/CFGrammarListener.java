package base;

import parsing.LR0Element;

import java.util.HashSet;
import java.util.Set;

public interface CFGrammarListener {
    void onChanged(Change change);

    enum ChangeType {
        startProductionAdded,
        enterGOTO,
        enterCLOSURE,
        endCLOSURE, addCLOSURE
    }

    class Change {
        private CFGrammar grammar;
        private CFProduction newProduction;

        private ChangeType type;

        // enterGOTO
        private Symbol enterGotoSymbol;

        // enterCLOSURE
        private Set<LR0Element> closureStartSet;

        // addCLOSURE
        private Set<LR0Element> closureNewElements;
        private Set<MetaSymbol> closureMetaSymbols;

        public Change() {
            closureNewElements = new HashSet<>();
            closureMetaSymbols = new HashSet<>();
        }

        public CFGrammar getGrammar() {
            return grammar;
        }

        public void setGrammar(CFGrammar grammar) {
            this.grammar = grammar;
        }

        public CFProduction getNewProduction() {
            return newProduction;
        }

        public void setNewProduction(CFProduction newProduction) {
            this.newProduction = newProduction;
        }

        public ChangeType getType() {
            return type;
        }

        public void setType(ChangeType type) {
            this.type = type;
        }

        public void setGotoSymbol(Symbol symbol) {
            enterGotoSymbol = symbol;
        }

        public Symbol getGotoSymbol() {
            return enterGotoSymbol;
        }

        public Set<LR0Element> getClosureStartSet() {
            return closureStartSet;
        }

        public void setClosureStartSet(Set<LR0Element> closureStartSet) {
            this.closureStartSet = closureStartSet;
        }

        public Set<LR0Element> getClosureNewElements() {
            return closureNewElements;
        }

        public Set<MetaSymbol> getClosureMetaSymbols() {
            return closureMetaSymbols;
        }

        public void setClosureNewElements(Set<LR0Element> closureNewElements) {
            this.closureNewElements = closureNewElements;
        }

        public void addClosureNewElement(MetaSymbol closureMetaSymbol, LR0Element closeNewElement) {
            closureMetaSymbols.add(closureMetaSymbol);
            closureNewElements.add(closeNewElement);
        }
    }
}
