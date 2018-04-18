package parsing;

import java.util.HashSet;
import java.util.Set;

public abstract class Symbol {
    public abstract char getRepresentation();

    public String toString() {
        return String.valueOf(this.getRepresentation());
    }

    public abstract Set<Symbol> getFIRST(CFGrammar grammar);
}
