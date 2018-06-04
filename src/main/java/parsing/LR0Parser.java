package parsing;

import base.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LR0Parser implements Parser {

    private MetaSymbol previousStartSymbol;

    @Override
    public StateAutomaton parse(CFGrammar grammar) {
        StateAutomaton stateAutomaton = new StateAutomaton();

        // 1. Add a new start production
        char newStartSymbolRepr = grammar.getFreeMetaSymbol("ZS");
        CFProduction newStartProduction = new CFProduction(newStartSymbolRepr, grammar.getStartSymbol().toString());
        grammar.addProduction(newStartProduction);
        previousStartSymbol = grammar.getStartSymbol();
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

    @Override
    public ParseTable generateTable(CFGrammar grammar, StateAutomaton stateAutomaton) {

        ParseTable parseTable = new ParseTable();

        State firstState = stateAutomaton.getStartState();

        LR0Element acceptRule = null;
        Integer acceptRuleStateNumber = null;
        boolean acceptRuleFound = false;

        // 1. Add shift entries
        for (int i = 0; i < stateAutomaton.size(); i++) {
            State state = stateAutomaton.getState(i);
            for (StateTransition transition : stateAutomaton.getTransitionsFrom(state.getNumber())) {

                ParserAction action = ParserAction.Null;
                if (transition.getSymbol() instanceof TerminalSymbol)
                    action = ParserAction.Shift;
                parseTable.add(
                        state.getNumber(),
                        transition.getSymbol(),
                        action,
                        transition.getToState()
                );
            }

            // find and remember the sythetic start rule (acceptRule) in advance for step 2
            if (!acceptRuleFound) {
                for (LR0Element element : state.getElements()) {
                    if (element.isAccepting()
                            && element.getSymbolBeforeMarker().equals(previousStartSymbol)) {
                        acceptRuleFound = true;
                        acceptRuleStateNumber = state.getNumber();
                        acceptRule = element;
                    }
                }
            }
        }

        if(!acceptRuleFound) throw new IllegalArgumentException(
                "No state with the accepting LR0-Element [Z -> S.] could be found in input stateAutomaton"
        );

        // 2. Add accept entry for the syntethic start rule
        parseTable.add(
                acceptRuleStateNumber,
                new TerminalSymbol('$'),
                ParserAction.Accept,
                null
        );

        for(int i = 0; i < stateAutomaton.size(); i++) {
            State state = stateAutomaton.getState(i);
            /*
                3. Add reduce entries
                for each accepting LR0Element (excluding acceptRule) in each state: add "reduce <prodNumber>"
                to ALL Terminalsymbols in the row which corresponds to the state
            */
            List<TerminalSymbol> terminalSymbols = grammar.getTerminalSymbols();
            terminalSymbols.add(new TerminalSymbol('$'));
            for(LR0Element element : state.getElements()) {
                if(element.isAccepting() && !element.equals(acceptRule)) {
                    for(TerminalSymbol terminalSymbol : terminalSymbols) {
                        parseTable.add(
                          state.getNumber(),
                          terminalSymbol,
                          ParserAction.Reduce,
                          findProduction(grammar, element)
                        );
                    }
                }
            }
            // possible optimization: let each state cache which elements end up with '' after marker
        }

        return parseTable;
    }

    // optimize: 1. let LR0Element know its productionNumber in Grammar
    // 2. let LR0Element remember the productionNumber it was generated from
    // 3. use it instead of calling this (expensive) function
    private Integer findProduction(CFGrammar grammar, LR0Element element) {
        Integer result = null;
        Iterator<CFProduction> iter = grammar.getProductionList().iterator();
        for(int i = 0; iter.hasNext(); i++) {
            CFProduction production = iter.next();
            if(production.equals(element.getProduction()))
                result = i;
        }

        return result;
    }
}
