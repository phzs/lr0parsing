package parsing;

public class MetaSymbol extends Symbol {

    private char representation;

    public MetaSymbol(char representation) {
        this.representation = representation;
    }

    @Override
    public char getRepresentation() {
        return Character.toUpperCase(this.representation);
    }
}
