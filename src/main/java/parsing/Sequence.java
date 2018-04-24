package parsing;

import java.util.ArrayList;
import java.util.List;

public class Sequence {
    private List<Symbol> symbols;

    public Sequence(String representation) {
        this.symbols = new ArrayList<Symbol>();
        for(char c : representation.toCharArray()) {
            if(Character.isUpperCase(c))
                this.symbols.add(new MetaSymbol(c));
            else
                this.symbols.add(new TerminalSymbol(c));
        }
    }

    public boolean applyLeftmostDerivation(CFProduction cfProduction) {
        boolean success = false;
        if(symbols.contains(cfProduction.getLeft())) {
            int position = symbols.indexOf(cfProduction.getLeft());
            symbols.remove(position);
            symbols.addAll(position, cfProduction.getRight().symbols);
            success = true;
        }
        return success;
    }

    public int getLength() {
        return this.symbols.size();
    }

    @Override
    public String toString() {
        return symbols.stream().map(Objects::toString).collect(Collectors.joining());
    }

    public void addSymbol(Symbol symbol) {
        this.symbols.add(symbol);
    }
}
