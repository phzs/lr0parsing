package parsing;

public class TerminalSymbol extends Symbol {

    private char representation;

    public TerminalSymbol(char representation) {
        this.representation = representation;
    }

    @Override
    public char getRepresentation() {
        return Character.toLowerCase(this.representation);
    }
}
