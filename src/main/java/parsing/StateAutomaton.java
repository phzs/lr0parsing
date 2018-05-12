package parsing;

import base.Symbol;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class StateAutomaton {
    private TreeMap<Integer, State> states;
    private TreeMap<Integer, Integer> stateNumberMapping; // maps State.number to State.elements.hashCode (key of states)
    private Set<StateTransition> transitions;
    private Integer nextFreeStateNumber;

    public StateAutomaton() {
        states = new TreeMap<>();
        stateNumberMapping = new TreeMap<>();
        transitions = new HashSet<>();
        nextFreeStateNumber = 0;
    }

    public Integer registerState(Set<LR0Element> elements) {
        Integer stateNumber;
        State existingState = states.get(elements.hashCode());
        if(existingState == null) {
            stateNumber = nextFreeStateNumber++;
            states.put(elements.hashCode(), new State(elements, stateNumber));
            stateNumberMapping.put(stateNumber, elements.hashCode());
        } else {
            stateNumber = existingState.getNumber();
        }
        return stateNumber;
    }

    public Integer findState(Set<LR0Element> elements) {
        State existingState = states.get(elements.hashCode());
        return existingState != null ? existingState.getNumber() : -1;
    }

    public void addTransition(Integer fromState, Integer toState, Symbol transitionSymbol) {
        transitions.add(new StateTransition(fromState, toState, transitionSymbol));
    }

    public State getState(int stateNumber) {
        Integer key = this.stateNumberMapping.get(stateNumber);
        return this.states.get(key);
    }

    public State getStartState() {
        return getState(0);
    }

    public int size() {
        return states.size();
    }
}
