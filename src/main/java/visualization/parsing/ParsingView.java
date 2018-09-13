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

import java.util.LinkedList;
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

    private List<Integer> highlightedProductions;
    private State highlightedState;
    private StateTransition highlightedTransition;

    public ParsingView(WebView targetWebView) {
        this.webView = targetWebView;
        highlightedProductions = new LinkedList<>();
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
            resetHighlightedState();
            executeScript("highlightNode("+state.getNumber()+")");
            highlightedState = state;
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
            resetHighlightedTransition();
            executeScript("highlightEdge("+transition.getFromState()+","+transition.getToState()+")");
            highlightedTransition = transition;
            executeScript("highlightNode("+transition.getToState()+")");
            highlightedState = new State(null, transition.getToState());
        }
    }

    private void highlightProduction(int id) {
        executeScript("highlightRule("+id+")");
        highlightedProductions.add(id);
    }

    private void resetHighlightedProductions() {
        for(int id : highlightedProductions)
            executeScript("unhighlightRule("+id+")");
        highlightedProductions.clear();
    }

    private void resetHighlightedState() {
        if(highlightedState != null) {
            executeScript("unhighlightNode(" + highlightedState.getNumber() + ")");
            highlightedState = null;
        }
    }

    private void resetHighlightedTransition() {
        if(highlightedTransition != null) {
            executeScript("unhighlightEdge(" + highlightedTransition.getFromState() + "," + highlightedTransition.getToState() + ")");
            highlightedTransition = null;
        }
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
                        });

                    } else if (change.getType() == ChangeType.enterCLOSURE) {
                        Platform.runLater(() -> {
                            webEngine.executeScript("setGcCLOSURE()");
                            executeScript("fadeElement(\"#gcRow\", true)");
                            addClosureEntries(change.getClosureStartSet());
                            executeScript("addGcLine()");
                            executeScript("highlightAllGcSymbols(highlightColor)");
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
                String script = "addParseTableEntry("
                        + stateId + ","                         // stateId
                        + "\'" + change.getKey() + "\',"        // symbol
                        + "\'" + change.getValueAdded() + "\'"  // entry
                        + ")";
                Platform.runLater(() -> {
                    executeScript(script);
                });
            }
        });
    }

    public void setVisibleParsingStep(ParsingStep step) {
        Platform.runLater(() -> {
            resetHighlighting();

            String script = "setStep(";
            if (step == ParsingStep.One) script += 1;
            else if (step == ParsingStep.Two) script += 2;
            else if (step == ParsingStep.Three) script += 3;
            else if (step == ParsingStep.Results) script += 4;
            script += ")";
            executeScript(script);
        });
    }

    private void resetHighlighting() {
        resetHighlightedProductions();
        resetHighlightedState();
        resetHighlightedTransition();
    }

    public void cleanForContinue() {
        resetHighlighting();
        executeScript("fadeElement(\"#gcTableRow\", false)");
    }
}
