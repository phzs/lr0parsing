package parsing;

import base.Symbol;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class State {
    private int number;
    private Set<LR0Element> elements;

    public State(Set<LR0Element> elements, int number) {
        this.elements = elements;
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Set<Symbol> getFollowingSymbols() {
        Set<Symbol> followingSymbols = new HashSet<>();
        for(LR0Element element : elements) {
            Symbol symbol = element.getSymbolRightOfMarker();
            if (symbol != null)
                followingSymbols.add(symbol);
        }
        return followingSymbols;
    }

    public Set<LR0Element> getElements() {
        return elements;
    }
}
