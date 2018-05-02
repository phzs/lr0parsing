package base;

public abstract class Symbol {
    public abstract char getRepresentation();

    public String toString() {
        return String.valueOf(this.getRepresentation());
    }
}
