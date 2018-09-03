package base;

public interface CFGrammarListener {
    void onChanged(Change change);

    enum ChangeType {
        startProductionAdded
    }

    class Change {
        private CFGrammar grammar;
        private CFProduction newProduction;

        private ChangeType type;

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
    }
}
