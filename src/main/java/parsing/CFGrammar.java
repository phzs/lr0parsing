package parsing;

import java.util.ArrayList;
import java.util.List;

public class CFGrammar {
    public List<CFProduction> getProductionList() {
        return productionList;
    }

    public void setProductionList(List<CFProduction> productionList) {
        this.productionList = productionList;
    }

    private List<CFProduction> productionList;

    public CFGrammar() {
        this.productionList = new ArrayList<CFProduction>();
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
}
