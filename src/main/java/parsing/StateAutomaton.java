package parsing;

import base.Symbol;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class StateAutomaton {
    private ObservableMap<Integer, State> states;
    private TreeMap<Integer, Integer> stateNumberMapping; // maps State.number to State.elements.hashCode (key of states)
    private ObservableSet<StateTransition> transitions;
    private Integer nextFreeStateNumber;

    public StateAutomaton() {
        states = new SimpleMapProperty<>(FXCollections.observableHashMap());
        stateNumberMapping = new TreeMap<>();
        transitions = new SimpleSetProperty<>(FXCollections.observableSet());
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

    public List<State> getStates() {
        LinkedList<State> result = new LinkedList<>();
        for(Integer key : stateNumberMapping.keySet())
            result.add(getState(key));
        return result;
    }

    public State getStartState() {
        return getState(0);
    }

    public int size() {
        return states.size();
    }

    public List<StateTransition> getTransitionsFrom(int startStateNumber) {
        List<StateTransition> result = new LinkedList<>();
        for(StateTransition transition : transitions) {
            if(transition.getFromState() == startStateNumber)
                result.add(transition);
        }
        return result;
    }

    public ObservableMap statesProperty() {
        return states;
    }

    public ObservableSet transitionsProperty() {
        return transitions;
    }

    /*
    public List<State> getStatesAscending() {
        List<State> result = new LinkedList<>();
        Iterator<Integer> iter = states.keySet().iterator(); // iterator preserves ascending order
        while(iter.hasNext()) {
            result.add(states.get(iter.next()));
        }
        return result;
    }
    */
}
