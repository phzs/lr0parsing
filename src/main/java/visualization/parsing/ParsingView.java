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
import visualization.parseTable.ParseTableCellIdentifier;

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

    private ParsingHighlightManager highlightManager;

    public ParsingView(WebView targetWebView) {
        this.webView = targetWebView;
        this.highlightManager = new ParsingHighlightManager(this);

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

    public void setAcceptingElement(LR0Element acceptingElement) {
        Platform.runLater(() -> {
            executeScript("setAcceptingElement(\""+acceptingElement+"\")");
        });
    }

    private void drawState(State state) {
        String content = "\""
                + state.toString().replace("\n", "\\n")
                + "\"";

        executeScript("addNode("+state.getNumber()+", "+content+")");

        if(StepController.getInstance().getLastCommand() != StepController.Command.Continue) {
            highlightManager.resetHighlightedStates();
            highlightManager.highlightState(state.getNumber());
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
            highlightManager.resetHighlightedTransitions();
            highlightManager.highlightTransition(transition);
            highlightManager.highlightState(transition.getToState());
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

    protected Object executeScript(String script) {
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
                        highlightManager.highlightProduction(0);
                    });

                }
                if(StepController.getInstance().getLastCommand() != StepController.Command.Continue) {
                    if (change.getType() == ChangeType.enterGOTO) {//TODO highlight symbols inside graph state
                        Platform.runLater(() -> {
                            executeScript("fadeElement(\"#gcRow\", true)");
                            executeScript("setGcGOTO(\"" + change.getGotoSymbol() + "\")");
                            highlightManager.resetHighlightedTransitions();
                            highlightManager.resetHighlightedStates();
                            highlightManager.highlightState(change.getGotoFromStateId());
                        });

                    } else if (change.getType() == ChangeType.enterCLOSURE) {
                        Platform.runLater(() -> {
                            webEngine.executeScript("setGcCLOSURE()");
                            executeScript("fadeElement(\"#gcRow\", true)");
                            addClosureEntries(change.getClosureStartSet());
                            executeScript("addGcLine()");
                            executeScript("highlightAllGcSymbols(highlightColor)");
                            highlightManager.resetHighlightedTransitions();
                        });

                    } else if (change.getType() == ChangeType.addCLOSURE) {
                        Platform.runLater(() -> {
                            List<CFProduction> grammarProductions = grammar.getProductionList();
                            Set<MetaSymbol> metaSymbols = change.getClosureMetaSymbols();
                            for(int i = 0; i < grammar.getProductionList().size(); i++) {
                                if(metaSymbols.contains(grammarProductions.get(i).getLeft()))
                                    highlightManager.highlightProduction(i+1);
                            }
                            addClosureEntries(change.getClosureNewElements());
                            for(Symbol symbol : metaSymbols)
                                executeScript("highlightGcSymbols(\'"+symbol+"\', \'#46b83a\')");
                        });

                    } else if (change.getType() == ChangeType.endCLOSURE) {
                        Platform.runLater(() -> {
                            executeScript("fadeElement(\"#gcRow\", false)");
                            executeScript("clearGc()");
                            highlightManager.resetHighlightedProductions();
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
                    ParserAction action = change.getValueAdded().getAction();
                    if(action == ParserAction.Shift)
                        executeScript("setStep3Substep(1,1)");
                    else if(action == ParserAction.Null)
                        executeScript("setStep3Substep(1,2)");
                    else if(action == ParserAction.Accept)
                        executeScript("setStep3Substep(1,3)");
                    else if(action == ParserAction.Reduce)
                        executeScript("setStep3Substep(2)");
                    String script = "addParseTableEntry("
                            + stateId + ","                         // stateId
                            + "\'" + change.getKey() + "\',"        // symbol
                            + "\'" + change.getValueAdded() + "\'"  // entry
                            + ")";
                    executeScript(script);

                    if(StepController.getInstance().getLastCommand() != StepController.Command.Continue) {
                        highlightManager.resetHighlightedStateNumRects();
                        highlightManager.setGraphHighlightedColor("#8fd563");
                        highlightManager.resetParseTableHighlighting();
                        highlightManager.highlightState(stateId);
                        highlightManager.highlightTransition(new StateTransition(stateId, change.getValueAdded().getNumber(), change.getKey()), "#3f96d7");

                        if(action == ParserAction.Shift || action == ParserAction.Null) {
                            highlightManager.highlightStateNum(+change.getValueAdded().getNumber(), "rgba(255, 2, 2, 0.6)");
                            highlightManager.highlightParseTableCell(
                                    new ParseTableCellIdentifier(
                                            stateId,
                                            change.getKey()),
                                    "rgba(255, 2, 2, 0.6)");
                        }

                        highlightManager.highlightParseTableRow(stateId);
                        highlightManager.highlightParseTableHeader(change.getKey());
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

    public void cleanForContinue() {
        highlightManager.resetGraphHighlighting();
        highlightManager.resetParseTableHighlighting();
        executeScript("fadeElement(\"#gcRow\", false)");
        executeScript("clearGc()");
    }
}
