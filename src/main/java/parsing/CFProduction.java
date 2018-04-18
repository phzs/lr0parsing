package parsing;

import java.util.LinkedList;
import java.util.List;

public class CFProduction {
    private MetaSymbol left;
    private List<Symbol> right;


    public CFProduction(MetaSymbol left, List<Symbol> right) {
        this.left = left;
        this.right = right;
    }
    public CFProduction(char left, String right) {
        this.left = new MetaSymbol(left);
        this.right = new LinkedList<Symbol>();

        for(char c : right.toCharArray()) {
            if(Character.isUpperCase(c))
                this.right.add(new MetaSymbol(c));
            else
                this.right.add(new TerminalSymbol(c));
        }

    }

    public MetaSymbol getLeft() {
        return left;
    }

    public void setLeft(MetaSymbol left) {
        this.left = left;
    }

    public List<Symbol> getRight() {
        return right;
    }

    public void setRight(List<Symbol> right) {
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
        return left.toString() + " --> " + symbolsToString(right);
    }
}
