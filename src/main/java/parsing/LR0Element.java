package parsing;


import java.lang.IllegalArgumentException;
import java.util.Objects;
import java.util.Set;

/**
 * A state while parsing
 *
 * Contains a marker which saves a position at the right side of the production.
 */
public class LR0Element {
    private CFProduction production;
    private int markerPosition;

    public LR0Element(CFProduction production, int markerPosition) {
        if(production != null && markerPosition >= 0) {
            this.production = production;
            this.markerPosition = markerPosition;
        } else
            throw new IllegalArgumentException("Erroneous arguments");
    }

    public LR0Element(char left, String right, int markerPosition) {
        this(new CFProduction(left, right), markerPosition);
    }

    public Symbol getSymbolRightOfMarker() {
        return production.getRight().get(markerPosition);
    }

    @Override
    public String toString() {
        String productionRight = production.getRight().toString();
        String newRight = productionRight.substring(0, markerPosition);
        newRight += ".";
        newRight += productionRight.substring(markerPosition);
        return "[" + production.getLeft().toString() + " --> " + newRight + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LR0Element that = (LR0Element) o;
        return markerPosition == that.markerPosition &&
                production.equals(that.production);
    }

    @Override
    public int hashCode() {
        return Objects.hash(production, markerPosition);
    }
}
