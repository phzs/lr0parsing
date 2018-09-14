package visualization.parsing;

import base.*;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.SetChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import parsing.*;
import visualization.StepController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParsingView {
    public static final double paddingLeft = 25.0;
    public static final double paddingTop = 25.0;

    private WebView webView;
    private WebEngine webEngine;

    private StateAutomaton automaton;
    private double nextX = paddingLeft;
    private double nextY = paddingTop;

    // highlighting for step 2 (automaton generation)
    private Set<Integer> highlightedProductions;
    private Set<Integer> highlightedStates;
    private Set<StateTransition> highlightedTransitions;

    // highlighting for step 3 (parse table generation)
    private Set<Integer> highlightedParseTableRows;
    private Set<Symbol> highlightedParseTableHeaders;

    public ParsingView(WebView targetWebView) {
        this.webView = targetWebView;
        highlightedProductions = new HashSet<>();
        highlightedStates = new HashSet<>();
        highlightedTransitions = new HashSet<>();
        highlightedParseTableRows = new HashSet<>();
        highlightedParseTableHeaders = new HashSet<>();

        initWebView();
    }

    public void setStateAutomaton(StateAutomaton stateAutomaton) {
        this.automaton = stateAutomaton;
        // handle future changes to stateAutomaton
        stateAutomaton.statesProperty().addListener(new MapChangeListener() {
            @Override
            public void onChanged(Change change) {
                if(change.wasAdded()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            drawState((State) change.getValueAdded());
                        }
                    });
                }
            }
        });

        stateAutomaton.transitionsProperty().addListener(new SetChangeListener() {
            @Override
            public void onChanged(Change change) {
                if(change.wasAdded()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            drawTransition((StateTransition) change.getElementAdded());
                        }
                    });
                }
            }
        });
    }

    private void initWebView() {
        webEngine = webView.getEngine();
        //webEngine.setUserStyleSheetLocation("data:,body { font: 12px Arial; }");
        webEngine.load(this.getClass().getResource("/webview.html").toExternalForm());
        JSObject window = (JSObject) executeScript("window");
        window.setMember("app", this);
    }

    // will be executed when the webview page has finished loading
    public void onWebviewPageLoaded() {
        System.out.println("Webview is now ready");
    }

    private void drawState(State state) {
        String content = "\""
                + state.toString().replace("\n", "\\n")
                + "\"";

        executeScript("addNode("+state.getNumber()+", "+content+")");

        if(StepController.getInstance().getLastCommand() != StepController.Command.Continue) {
            resetHighlightedStates();
            highlightState(state.getNumber());
        }
    }

    private void drawTransition(StateTransition transition) {
        State from = this.automaton.getState(transition.getFromState());
        State to = this.automaton.getState(transition.getToState());

        String transitionLabel = "\""
                + transition.getSymbol().toString()
                + "\"";
        executeScript("addEdge("+from.getNumber()+","+to.getNumber()+", "+ transitionLabel +")");

        if(StepController.getInstance().getLastCommand() != StepController.Command.Continue) {
            resetHighlightedTransitions();
            highlightTransition(transition);
            highlightState(transition.getToState());
        }
    }

    private void highlightProduction(int id) {
        executeScript("highlightRule("+id+")");
        highlightedProductions.add(id);
    }

    private void highlightState(int id) {
        highlightState(id, "");
    }
    private void highlightState(int id, String color) {
        executeScript("highlightNode("+id+", \""+color+"\")");
        highlightedStates.add(id);
    }

    private void highlightTransition(StateTransition transition) {
        highlightTransition(transition, "");
    }

    private void highlightTransition(StateTransition transition, String color) {
        executeScript("highlightEdge(" + transition.getFromState() + "," + transition.getToState() + ", \""+color+"\")");
        highlightedTransitions.add(transition);
    }

    private void highlightParseTableRow(int stateId) {
        executeScript("highlightParseTableRow(" + stateId + ")");
        highlightedParseTableRows.add(stateId);
    }

    private void highlightParseTableHeader(Symbol symbol) {
        executeScript("highlightParseTableHeader(\'" + symbol + "\', \"blue\")");
        highlightedParseTableHeaders.add(symbol);
    }

    private void resetHighlightedProductions() {
        for(int id : highlightedProductions)
            executeScript("unhighlightRule("+id+")");
        highlightedProductions.clear();
    }

    private void resetHighlightedStates() {
        for(int id : highlightedStates)
            executeScript("unhighlightNode(" + id + ")");
        highlightedStates.clear();
    }

    private void resetHighlightedTransitions() {
        for(StateTransition transition : highlightedTransitions)
            executeScript("unhighlightEdge(" + transition.getFromState() + "," + transition.getToState() + ")");
        highlightedTransitions.clear();
    }

    private void resetHighlightedParseTableRows() {
        for(int stateId : highlightedParseTableRows)
            executeScript("unhighlightParseTableRow("+stateId+")");
        highlightedParseTableRows.clear();
    }

    private void resetHighlightedParseTableHeaders() {
        for(Symbol symbol : highlightedParseTableHeaders)
            executeScript("unhighlightParseTableHeader(\'"+symbol+"\')");
        highlightedParseTableHeaders.clear();
    }

    /**
     * Sets webView and moves all drawn nodes to webView
     * (Nodes can only have one parent and lose the binding to their previous parent)
     * @param targetPane
     */
    public void setTargetPane(Pane targetPane) {

    }
    
    public void reset() {
        executeScript("clearGraph()");
        executeScript("clearRules()");
        executeScript("clearParseTable()");
        setVisibleParsingStep(ParsingStep.One);
    }

    private Object executeScript(String script) {
        System.out.println("[ParsingView,"+Thread.currentThread()+"] execute: " + script);
        return webEngine.executeScript(script);
    }

    public void initGrammar(CFGrammar grammar) {
        executeScript("clearRules()");
        List<CFProduction> productionList = grammar.getProductionList();
        for(int i = 0; i < productionList.size(); i++) {
            CFProduction production = productionList.get(i);
            String script = "addRule(" + i + ", \"" + production.getLeft() + "\", \"" + production.getRight()+"\")";
            executeScript(script);
        }

        /*
            add listener to get notified about the new production
            which will be added by the parser
        */
        grammar.addListener(new CFGrammarListener() {
            @Override
            public void onChanged(Change change) {
                if (change.getType() == ChangeType.startProductionAdded) {
                    CFProduction production = change.getNewProduction();
                    Platform.runLater(() -> {
                        String script = "insertFirstRule("
                                + "\'" + production.getLeft() + "\'"
                                + ", "
                                + "\'" + production.getRight() + "\'"
                                + ")";
                        executeScript(script);
                        highlightProduction(0);
                    });

                }
                if(StepController.getInstance().getLastCommand() != StepController.Command.Continue) {
                    if (change.getType() == ChangeType.enterGOTO) {//TODO highlight symbols inside graph state
                        Platform.runLater(() -> {
                            executeScript("fadeElement(\"#gcRow\", true)");
                            executeScript("setGcGOTO(\"" + change.getGotoSymbol() + "\")");
                            resetHighlightedTransitions();
                            resetHighlightedStates();
                            highlightState(change.getGotoFromStateId());
                        });

                    } else if (change.getType() == ChangeType.enterCLOSURE) {
                        Platform.runLater(() -> {
                            webEngine.executeScript("setGcCLOSURE()");
                            executeScript("fadeElement(\"#gcRow\", true)");
                            addClosureEntries(change.getClosureStartSet());
                            executeScript("addGcLine()");
                            executeScript("highlightAllGcSymbols(highlightColor)");
                            resetHighlightedTransitions();
                        });

                    } else if (change.getType() == ChangeType.addCLOSURE) {
                        Platform.runLater(() -> {
                            List<CFProduction> grammarProductions = grammar.getProductionList();
                            Set<MetaSymbol> metaSymbols = change.getClosureMetaSymbols();
                            for(int i = 0; i < grammar.getProductionList().size(); i++) {
                                if(metaSymbols.contains(grammarProductions.get(i).getLeft()))
                                    highlightProduction(i+1);
                            }
                            addClosureEntries(change.getClosureNewElements());
                            for(Symbol symbol : metaSymbols)
                                executeScript("highlightGcSymbols(\'"+symbol+"\', \'#46b83a\')");
                        });

                    } else if (change.getType() == ChangeType.endCLOSURE) {
                        Platform.runLater(() -> {
                            executeScript("fadeElement(\"#gcRow\", false)");
                            executeScript("clearGc()");
                            resetHighlightedProductions();
                        });
                    }
                }
            }
        });
    }

    private void addClosureEntries(Set<LR0Element> elements) {
        for(LR0Element element : elements) {
            executeScript("addGcEntry(\"" + element + "\")");
        }
    }

    private String listToJsArray(List<? extends Symbol> list) {
        String result = "[";
        for(Symbol symbol : list) {
            result += ("\"" + symbol.toString() + "\", ");
        }
        result += "]";
        return result;
    }

    public void initParseTable(List<TerminalSymbol> terminalSymbols, List<MetaSymbol> metaSymbols) {
        terminalSymbols.add(new TerminalSymbol('$'));
        String script = "initParseTable("
                + listToJsArray(terminalSymbols)
                + ", "
                + listToJsArray(metaSymbols)
                +")";
        executeScript(script);
    }

    public void addParseTableEntryListener(int stateId, ObservableMap<Symbol, ParseTable.TableEntry> entryObservableMap) {
        entryObservableMap.addListener(new MapChangeListener<Symbol, ParseTable.TableEntry>() {
            @Override
            public void onChanged(Change<? extends Symbol, ? extends ParseTable.TableEntry> change) {
                Platform.runLater(() -> {
                    String script = "addParseTableEntry("
                            + stateId + ","                         // stateId
                            + "\'" + change.getKey() + "\',"        // symbol
                            + "\'" + change.getValueAdded() + "\'"  // entry
                            + ")";
                    executeScript(script);

                    if(StepController.getInstance().getLastCommand() != StepController.Command.Continue) {
                        setGraphHighlightedColor("#8fd563");
                        resetParseTableHighlighting();
                        highlightState(stateId);
                        highlightTransition(new StateTransition(stateId, change.getValueAdded().getNumber(), null), "#3f96d7");

                        highlightParseTableRow(stateId);
                        highlightParseTableHeader(change.getKey());
                    }
                });
            }
        });
    }

    public void setVisibleParsingStep(ParsingStep step) {
        Platform.runLater(() -> {
            cleanForContinue();

            String script = "setStep(";
            if (step == ParsingStep.One) script += 1;
            else if (step == ParsingStep.Two) script += 2;
            else if (step == ParsingStep.Three) script += 3;
            else if (step == ParsingStep.Results) script += 4;
            script += ")";
            executeScript(script);
        });
    }

    private void setGraphHighlightedColor(String color) {
        for(StateTransition transition : highlightedTransitions)
            highlightTransition(transition, color);
        for(Integer stateId : highlightedStates)
            highlightState(stateId, color);
    }

    private void resetGraphHighlighting() {
        resetHighlightedProductions();
        resetHighlightedStates();
        resetHighlightedTransitions();
    }
    private void resetParseTableHighlighting() {
        resetHighlightedParseTableRows();
        resetHighlightedParseTableHeaders();
    }

    public void cleanForContinue() {
        resetGraphHighlighting();
        resetParseTableHighlighting();
        executeScript("fadeElement(\"#gcRow\", false)");
    }
}
