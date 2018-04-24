package parsing;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Sequence {
    private List<Symbol> symbols;

    public Sequence() {
        this.symbols = new ArrayList<>();
    }

    public Sequence(String representation) {
        this();
        for(char c : representation.toCharArray()) {
            if(Character.isUpperCase(c))
                this.symbols.add(new MetaSymbol(c));
            else
                this.symbols.add(new TerminalSymbol(c));
        }
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<Symbol> symbols) {
        this.symbols = symbols;
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

    @JsonIgnore
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sequence sequence = (Sequence) o;
        return Objects.equals(symbols, sequence.symbols);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbols);
    }
}
