package parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CFGrammar {
    private List<CFProduction> productionList;
    private MetaSymbol startSymbol;

    public CFGrammar(MetaSymbol startSymbol) {
        this.startSymbol = startSymbol;
        this.productionList = new ArrayList<>();
    }

    public CFGrammar(char startSymbol) {
        this(new MetaSymbol(startSymbol));
    }

    public CFGrammar() {
        this(null);
    }

    public List<CFProduction> getProductionList() {
        return productionList;
    }

    public void setProductionList(List<CFProduction> productionList) {
        this.productionList = productionList;
    }

    public MetaSymbol getStartSymbol() {
        return startSymbol;
    }

    public void setStartSymbol(MetaSymbol startSymbol) {
        this.startSymbol = startSymbol;
    }

    public void addProduction(CFProduction cfProduction) {
        this.productionList.add(cfProduction);
    }

    @Override
    public String toString() {
        String result = "";
        int i = 0;
        for(CFProduction p : productionList) {
            if(i != 0) result += "\n";
            result += "("+i+++") ";
            result += p.toString();
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CFGrammar cfGrammar = (CFGrammar) o;
        return Objects.equals(productionList, cfGrammar.productionList) &&
                Objects.equals(startSymbol, cfGrammar.startSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productionList, startSymbol);
    }
}
