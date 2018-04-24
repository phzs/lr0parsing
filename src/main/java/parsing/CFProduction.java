package parsing;

import java.util.LinkedList;
import java.util.List;

public class CFProduction {
    private MetaSymbol left;
    private Sequence right;


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
}
