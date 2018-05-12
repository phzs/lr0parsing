package base;

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
    public Symbol get(int position) {
        return symbols.get(position);
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

    public Sequence subsequence(int begin) {
        Sequence result = new Sequence();
        List<Symbol> newSymbols = symbols
                .stream()
                .skip(begin)
                .collect(Collectors.toList());
        result.setSymbols(newSymbols);
        return result;

    }

    public Sequence subsequence(int begin, int end) {
        Sequence result = new Sequence();
        List<Symbol> newSymbols = symbols
                .stream()
                .skip(begin)
                .limit(end-begin)
                .collect(Collectors.toList());
        result.setSymbols(newSymbols);
        assert(result.getLength() == Math.min(getLength(), end) - begin);
        return result;
    }

    /**
     * Get a list of all MetaSymbols in this Sequence, in order of
     * appearance.
     * @return List of MetaSymbols
     */
    @JsonIgnore
    public List<MetaSymbol> getMetaSymbols() {
        return symbols
                .stream()
                .filter(s -> s instanceof MetaSymbol)
                .map(s -> (MetaSymbol) s)
                .collect(Collectors.toList());
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

    public int size() {
        return this.symbols.size();
    }
}
