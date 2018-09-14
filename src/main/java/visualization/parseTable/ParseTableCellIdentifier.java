package visualization.parseTable;

import base.Symbol;

import java.util.Objects;

public class ParseTableCellIdentifier {
    public final int stateId;
    public final Symbol symbol;
    public ParseTableCellIdentifier(int stateId, Symbol symbol) {
        this.stateId = stateId;
        this.symbol = symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParseTableCellIdentifier that = (ParseTableCellIdentifier) o;
        return stateId == that.stateId &&
                Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateId, symbol);
    }
}
