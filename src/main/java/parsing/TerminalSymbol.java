package parsing;

import java.util.HashSet;
import java.util.Set;

public class TerminalSymbol extends Symbol {

    private char representation;

    public TerminalSymbol(char representation) {
        this.representation = representation;
    }

    @Override
    public char getRepresentation() {
        return Character.toLowerCase(this.representation);
    }

    @Override
    public Set<Symbol> getFIRST(CFGrammar grammar) {
        Set<Symbol> result = new HashSet<>();
        result.add(this);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerminalSymbol that = (TerminalSymbol) o;
        return representation == that.representation;
    }

    @Override
    public int hashCode() {
        return Character.hashCode(representation);
    }
}
