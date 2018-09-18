package base;

public class MetaSymbol extends Symbol {

    private char representation;

    public MetaSymbol() {

    }

    public MetaSymbol(char representation) {
        this.representation = representation;
    }

    public void setRepresentation(char representation) {
        this.representation = representation;
    }

    public boolean validate() {
        return (""+representation).matches("^[A-Z]$");
    }

    @Override
    public char getRepresentation() {
        return Character.toUpperCase(this.representation);
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
