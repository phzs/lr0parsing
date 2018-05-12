package parsing;

import base.CFGrammar;
import base.CFProduction;
import base.MetaSymbol;
import base.Symbol;

import java.util.HashSet;
import java.util.Set;

public class LR0Parser implements Parser {

    @Override
    public StateAutomaton parse(CFGrammar grammar) {
        StateAutomaton stateAutomaton = new StateAutomaton();

        // 1. Add a new start production
        char newStartSymbolRepr = grammar.getFreeMetaSymbol("ZS");
        CFProduction newStartProduction = new CFProduction(newStartSymbolRepr, grammar.getStartSymbol().toString());
        grammar.addProduction(newStartProduction);
        grammar.setStartSymbol(new MetaSymbol(newStartSymbolRepr));

        // 2. Add state for start production
        Set<LR0Element> startStateSet = grammar.getCLOSURE(newStartProduction.getLR0Element(0));
        int startStateId = stateAutomaton.registerState(startStateSet);

        // 3. For each symbol following a '.' in the set of LR0Elements of the start state: compute GOTO_0 and register a state+transition
        // 4. Repeat this for all states
        Set<Integer> statesToProcess = new HashSet<>();
        Set<Integer> statesProcessed = new HashSet<>();
        statesToProcess.add(startStateId);
        while(statesToProcess.size() > 0) {
            int stateId = statesToProcess.iterator().next();
            Set<Symbol> symbolsToProcess = stateAutomaton.getState(stateId).getFollowingSymbols();
            for (Symbol symbol : symbolsToProcess) {
                Set<LR0Element> symbolGOTO = grammar.getGOTO(stateAutomaton.getState(stateId).getElements(), symbol);
                int newStateId = stateAutomaton.registerState(symbolGOTO);
                if(!statesProcessed.contains(newStateId))
                    statesToProcess.add(newStateId);
                stateAutomaton.addTransition(stateId, newStateId, symbol);
            }
            statesProcessed.add(stateId);
            statesToProcess.remove(stateId);
        }

        return stateAutomaton;
    }
}
