package base;

public abstract class Symbol implements Comparable<Symbol> {
    public abstract char getRepresentation();

    public String toString() {
        return String.valueOf(this.getRepresentation());
    }

    @Override
    public int compareTo(Symbol o) {
        int result = 0;
        if(o.getRepresentation() != getRepresentation()) {
            result = -Character.compare(o.getRepresentation(), getRepresentation());
            if(o instanceof TerminalSymbol && this instanceof MetaSymbol) {
                result = 1;
            } else if(o instanceof MetaSymbol && this instanceof TerminalSymbol) {
                result = -1;
            } else if(o instanceof TerminalSymbol && this instanceof TerminalSymbol) {
                if(o.getRepresentation() == '$') result = -1;
                else if(getRepresentation() == '$') result = 1;
            }
        }
        return result;
    }
}
