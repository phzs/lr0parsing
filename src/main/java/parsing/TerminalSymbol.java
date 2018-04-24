package parsing;

public class TerminalSymbol extends Symbol {

    private char representation;

    public TerminalSymbol() {

    }

    public TerminalSymbol(char representation) {
        this.representation = representation;
    }

    public void setRepresentation(char representation) {
        this.representation = representation;
    }

    @Override
    public char getRepresentation() {
        return Character.toLowerCase(this.representation);
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
