package base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import parsing.LR0Element;

import java.util.*;

public class CFGrammar {
    private List<CFProduction> productionList;
    private Map<MetaSymbol, List<CFProduction>> productionsByLeft;
    private MetaSymbol startSymbol;

    public CFGrammar(MetaSymbol startSymbol) {
        this.startSymbol = startSymbol;
        this.productionList = new ArrayList<>();
        this.productionsByLeft = new HashMap<>();
    }

    public CFGrammar(char startSymbol) {
        this(new MetaSymbol(startSymbol));
    }

    public CFGrammar() {
        this(null);
    }

    @JsonIgnore
    public Set<LR0Element> getCLOSURE(LR0Element element) {
        Set<LR0Element> elements = new HashSet<>();
        elements.add(element);
        return getCLOSURE(elements);
    }

    @JsonIgnore
    public Set<LR0Element> getCLOSURE(Set<LR0Element> elements) {
        Set<LR0Element> closure = new HashSet<>();
        // step 1: add the element itself
        closure.addAll(elements);

        // step 2: process all MetaSymbols that occur right of the marker in any
        //   element inside closure (repeat this step until nothing changes)
        boolean hasChanged;
        do {
            hasChanged = false;
            Set<LR0Element> elementsToAdd = new HashSet<>();
            for (LR0Element el : closure) {
                Symbol symbolRightOfMarker =  el.getSymbolRightOfMarker();
                if(symbolRightOfMarker instanceof MetaSymbol) {
                    MetaSymbol metaSymbol = (MetaSymbol) symbolRightOfMarker;
                    for (CFProduction cfProduction : productionsByLeft.get(metaSymbol)) {
                        LR0Element newElement = cfProduction.getLR0Element(0);
                        if(!closure.contains(newElement))
                            elementsToAdd.add(newElement);
                    }
                }
            }
            if(elementsToAdd.size() > 0) {
                closure.addAll(elementsToAdd);
                hasChanged = true;
            }
        } while(hasChanged);

        return closure;
    }

    @JsonIgnore
    public Set<LR0Element> getGOTO(Set<LR0Element> elements, Symbol readSymbol) {
        Set<LR0Element> closureInput = new HashSet<>();
        for(LR0Element el : elements) {
            Symbol rightOfMarker = el.getSymbolRightOfMarker();
            if(rightOfMarker != null && rightOfMarker.equals(readSymbol)) {
                closureInput.add(new LR0Element(el, 1));
            }
        }

        return getCLOSURE(closureInput);
    }

    public List<CFProduction> getProductionList() {
        return productionList;
    }

    private void addProductionByLeft(MetaSymbol key, CFProduction cfProduction) {
        if(productionsByLeft.get(key) == null)
            productionsByLeft.put(key, new LinkedList<>());
        this.productionsByLeft.get(key)
                .add(cfProduction);
    }

    public void setProductionList(List<CFProduction> productionList) {
        this.productionList = productionList;
        for(CFProduction newProduction : productionList) {
            MetaSymbol key = newProduction.getLeft();
            addProductionByLeft(key, newProduction);
        }
    }

    public MetaSymbol getStartSymbol() {
        return startSymbol;
    }

    public void setStartSymbol(MetaSymbol startSymbol) {
        this.startSymbol = startSymbol;
    }

    public void addProduction(CFProduction cfProduction) {
        this.productionList.add(cfProduction);
        addProductionByLeft(cfProduction.getLeft(), cfProduction);
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
