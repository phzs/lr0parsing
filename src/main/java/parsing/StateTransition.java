package parsing;

import base.Symbol;

import java.util.Objects;

public class StateTransition {
    private Integer fromState;
    private Integer toState;
    private Symbol symbol;

    public StateTransition(Integer fromState, Integer toState, Symbol transitionSymbol) {
        this.fromState = fromState;
        this.toState = toState;
        this.symbol = transitionSymbol;
    }

    public Integer getFromState() {
        return fromState;
    }

    public Integer getToState() {
        return toState;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateTransition that = (StateTransition) o;
        return Objects.equals(fromState, that.fromState) &&
                Objects.equals(toState, that.toState) &&
                Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromState, toState, symbol);
    }

    @Override
    public String toString() {
        return fromState + "--(" + symbol.toString() + ")-->" + toState;
    }
}
