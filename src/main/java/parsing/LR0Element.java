package parsing;


import base.CFProduction;
import base.Symbol;

import java.util.Objects;
import java.util.regex.Pattern;

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

    /**
     * copy constructor with offset
     */
    public LR0Element(LR0Element aLR0Element, int markerOffset) {
        this(aLR0Element.production, aLR0Element.markerPosition + markerOffset);
    }

    public LR0Element(char left, String right, int markerPosition) {
        this(new CFProduction(left, right), markerPosition);
    }

    public LR0Element(char left, String rightDotNotation) {
        String right = rightDotNotation;
        int markerPosition = rightDotNotation.indexOf('.');
        boolean invalidNumberOfDots = markerPosition == -1;
        right = right.replaceFirst(Pattern.quote("."), "");
        invalidNumberOfDots = invalidNumberOfDots || (right.indexOf('.') != -1);
        if(invalidNumberOfDots)
            throw new IllegalArgumentException("");
        else {
            this.production = new CFProduction(left, right);
            this.markerPosition = markerPosition;
        }
    }

    public Symbol getSymbolRightOfMarker() {
        if(production.getRight().size() > markerPosition)
            return production.getRight().get(markerPosition);
        else
            return null;
    }

    public boolean isAccepting() {
        return production.getRight().size() <= markerPosition;
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

    public Symbol getSymbolBeforeMarker() {
        Symbol result = null;
        if(markerPosition > 0) {
            result = production.getRight().get(markerPosition-1);
        }
        return result;
    }

    public CFProduction getProduction() {
        return production;
    }
}
