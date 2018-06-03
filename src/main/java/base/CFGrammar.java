package base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import parsing.LR0Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
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

    @JsonIgnore
    public char getFreeMetaSymbol() {
        return getFreeMetaSymbol("");
    }

    @JsonIgnore
    public char getFreeMetaSymbol(String desiredRepresentations) {
        char representation = '#';
        boolean desiredFree = false;
        List<Character> freeRepresentations = new LinkedList<>();
        for(int i = (int)'A'; i <= (int)'Z'; i++) {
            freeRepresentations.add((char) i);
        }
        for(CFProduction prod : productionList) {
            int index = freeRepresentations.indexOf(prod.getLeft().getRepresentation());
            if (index != -1) freeRepresentations.remove(index);
            for(Symbol toRemove : prod.getRight().getMetaSymbols()) {
                index = freeRepresentations.indexOf(toRemove.getRepresentation());
                if (index != -1) freeRepresentations.remove(index);
            }
        }
        if(desiredRepresentations.length() > 0 && freeRepresentations.size() > 0) {
            for(char desiredChar : desiredRepresentations.toCharArray()) {
                if(freeRepresentations.contains(desiredChar)) {
                    representation = desiredChar;
                    desiredFree = true;
                    break;
                }
            }
        }
        if(!desiredFree && freeRepresentations.size() > 0) {
            representation = freeRepresentations.get(freeRepresentations.size()-1);
        }
        return representation;
    }

    @JsonIgnore
    public List<TerminalSymbol> getTerminalSymbols() {
        List<TerminalSymbol> result = new LinkedList<>();
        Set<TerminalSymbol> uniqueSet = new HashSet<>();
        for(CFProduction production : productionList) {
            for(Symbol symbol : production.getRight().getSymbols()) {
                // this could be delegated to Sequence.getTerminalSymbols()
                if(symbol instanceof TerminalSymbol)
                    uniqueSet.add((TerminalSymbol) symbol);
            }
        }
        result.addAll(uniqueSet);
        Collections.sort(result);
        return result;
    }

    @JsonIgnore
    public List<MetaSymbol> getMetaSymbols() {
        List<MetaSymbol> result = new LinkedList<>();
        Set<MetaSymbol> uniqueSet = new HashSet<>();
        for(CFProduction production : productionList) {
            for(Symbol symbol : production.getRight().getSymbols()) {
                if(symbol instanceof MetaSymbol)
                    uniqueSet.add((MetaSymbol) symbol);
            }
            uniqueSet.add(production.getLeft());
        }
        result.addAll(uniqueSet);
        Collections.sort(result);
        return result;
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping(
                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        return objectMapper;
    }

    public static CFGrammar fromFile(File file) throws IOException {
        CFGrammar result = new CFGrammar();
        ObjectMapper objectMapper = getObjectMapper();
        String grammarJSON = FileUtils.readFileToString(file);
        return objectMapper.readValue(grammarJSON, CFGrammar.class);
    }

    public String toJSON() throws JsonProcessingException {
        return getObjectMapper().writeValueAsString(this);
    }
}
