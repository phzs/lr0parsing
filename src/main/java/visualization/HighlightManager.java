package visualization;

import base.Symbol;
import parsing.StateTransition;
import visualization.View;
import visualization.parseTable.ParseTableCellIdentifier;

import java.util.HashSet;
import java.util.Set;

public class HighlightManager {
    private View view;
    // highlighting for parsing step 2 (automaton generation)
    private Set<Integer> highlightedProductions;
    private Set<Integer> highlightedStates;
    private Set<StateTransition> highlightedTransitions;

    // highlighting for parsing step 3 (parse table generation)
    private Set<Integer> highlightedParseTableRows;
    private Set<Symbol> highlightedParseTableHeaders;
    private Set<ParseTableCellIdentifier> highlightedParseTableCells;
    private Set<Integer> highlightedStateNumRects;

    public HighlightManager(View view) {
        this.view = view;
        highlightedProductions = new HashSet<>();
        highlightedStates = new HashSet<>();

        highlightedTransitions = new HashSet<>();
        highlightedParseTableRows = new HashSet<>();
        highlightedParseTableHeaders = new HashSet<>();
        highlightedParseTableCells = new HashSet<>();
        highlightedStateNumRects = new HashSet<>();
    }

    private void executeScript(String script) {
        view.executeScript(script);
    }

    public void highlightProduction(int id) {
        executeScript("highlightRule("+id+")");
        highlightedProductions.add(id);
    }

    public void highlightState(int id) {
        highlightState(id, "");
    }
    public void highlightState(int id, String color) {
        executeScript("highlightNode("+id+", \""+color+"\")");
        highlightedStates.add(id);
    }
    public void highlightStateNum(int id, String color) {
        executeScript("highlightStateNumRect("+id+", \""+color+"\")");
        highlightedStateNumRects.add(id);
    }

    public void highlightParseTableCell(ParseTableCellIdentifier id, String styleClass) {
        executeScript("highlightParseTableCell("
                + id.stateId
                + ",\""+ id.symbol +"\", \""+styleClass+"\")");
        highlightedParseTableCells.add(id);
    }

    public void highlightTransition(StateTransition transition) {
        highlightTransition(transition, "");
    }

    public void highlightTransition(StateTransition transition, String color) {
        executeScript("highlightEdge(" + transition.getFromState() + "," + transition.getToState() + ", \""+color+"\")");
        highlightedTransitions.add(transition);
    }

    public void highlightParseTableRow(int stateId) {
        executeScript("highlightParseTableRow(" + stateId + ")");
        highlightedParseTableRows.add(stateId);
    }

    public void highlightParseTableHeader(Symbol symbol) {
        executeScript("highlightParseTableHeader(\'" + symbol + "\', \"blue\")");
        highlightedParseTableHeaders.add(symbol);
    }

    public void resetHighlightedProductions() {
        for(int id : highlightedProductions)
            executeScript("unhighlightRule("+id+")");
        highlightedProductions.clear();
    }

    public void resetHighlightedStates() {
        for(int id : highlightedStates)
            executeScript("unhighlightNode(" + id + ")");
        highlightedStates.clear();
    }

    public void resetHighlightedStateNumRects() {
        for(int id : highlightedStateNumRects) {
            if(!highlightedStates.contains(id)) {
                executeScript("unhighlightStateNumRect("+id+")");
            }
        }
    }

    public void resetHighlightedParseTableCells() {
        for(ParseTableCellIdentifier id : highlightedParseTableCells)
            executeScript("unhighlightParseTableCell("
                    + id.stateId
                    + ",\""+ id.symbol +"\")");
        highlightedParseTableCells.clear();
    }

    public void resetHighlightedTransitions() {
        for(StateTransition transition : highlightedTransitions)
            executeScript("unhighlightEdge(" + transition.getFromState() + "," + transition.getToState() + ")");
        highlightedTransitions.clear();
    }

    public void resetHighlightedParseTableRows() {
        for(int stateId : highlightedParseTableRows)
            executeScript("unhighlightParseTableRow("+stateId+")");
        highlightedParseTableRows.clear();
    }

    public void resetHighlightedParseTableHeaders() {
        for(Symbol symbol : highlightedParseTableHeaders)
            executeScript("unhighlightParseTableHeader(\'"+symbol+"\')");
        highlightedParseTableHeaders.clear();
    }

    public void highlightStackItems(int j, String styleClass) {
        executeScript("highlightStackItem("+j+", \""+styleClass+"\")");
    }
    public void resetHighlightedStackItems() {
        executeScript("unhighlightStackItems()");
    }
    public void highlightAnalysisInputNextSymbol(String styleClass) {
        executeScript("highlightInputNext(\""+styleClass+"\")");
    }
    public void resetHighlightedAnalysisInputNextSymbol() {
        executeScript("unhighlightInputNext()");
    }

    public void setGraphHighlightedColor(String color) {
        for(StateTransition transition : highlightedTransitions)
            highlightTransition(transition, color);
        for(Integer stateId : highlightedStates)
            highlightState(stateId, color);
    }

    public void resetGraphHighlighting() {
        resetHighlightedProductions();
        resetHighlightedStates();
        resetHighlightedTransitions();
    }
    public void resetParseTableHighlighting() {
        resetHighlightedParseTableRows();
        resetHighlightedParseTableHeaders();
        resetHighlightedParseTableCells();
    }
}
