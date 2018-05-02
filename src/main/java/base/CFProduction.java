package base;

import parsing.LR0Element;

import java.util.List;
import java.util.Objects;

public class CFProduction {
    private MetaSymbol left;
    private Sequence right;


    public CFProduction() {

    }
    public CFProduction(MetaSymbol left, Sequence right) {
        this.left = left;
        this.right = right;
    }
    public CFProduction(char left, String right) {
        this.left = new MetaSymbol(left);
        this.right = new Sequence();

        for(char c : right.toCharArray()) {
            if(Character.isUpperCase(c))
                this.right.addSymbol(new MetaSymbol(c));
            else
                this.right.addSymbol(new TerminalSymbol(c));
        }

    }

    public MetaSymbol getLeft() {
        return left;
    }

    public void setLeft(MetaSymbol left) {
        this.left = left;
    }

    public Sequence getRight() {
        return right;
    }

    public void setRight(Sequence right) {
        this.right = right;
    }

    private String symbolsToString(List<Symbol> symbols) {
        String result = "";
        for(Symbol symbol : symbols) {
            result += symbol.toString();
        }
        return result;
    }

    public String toString() {
        return left.toString() + " --> " + right.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CFProduction that = (CFProduction) o;
        return Objects.equals(left, that.left) &&
                Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    public LR0Element getLR0Element(int i) {
        return new LR0Element(this, i);
    }
}
