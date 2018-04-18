package parsing;

import java.util.HashSet;
import java.util.Set;

public class MetaSymbol extends Symbol {

    private char representation;

    public MetaSymbol(char representation) {
        this.representation = representation;
    }

    @Override
    public char getRepresentation() {
        return Character.toUpperCase(this.representation);
    }

    @Override
    public Set<Symbol> getFIRST(CFGrammar grammar) {
        Set<Symbol> result = new HashSet<>();

        //TODO

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaSymbol that = (MetaSymbol) o;
        return representation == that.representation;
    }

    @Override
    public int hashCode() {
        return Character.hashCode(representation);
    }
}
