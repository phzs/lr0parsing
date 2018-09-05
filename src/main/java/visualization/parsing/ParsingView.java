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
import parsing.ParseTable;
import parsing.State;
import parsing.StateAutomaton;
import parsing.StateTransition;

import java.util.List;
import java.util.ListIterator;

public class ParsingView {
    public static final double paddingLeft = 25.0;
    public static final double paddingTop = 25.0;

    private WebView webView;
    private WebEngine webEngine;

    private StateAutomaton automaton;
    private double nextX = paddingLeft;
    private double nextY = paddingTop;

    public ParsingView(WebView targetWebView) {
        this.webView = targetWebView;

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
    }

    private void drawTransition(StateTransition transition) {
        State from = this.automaton.getState(transition.getFromState());
        State to = this.automaton.getState(transition.getToState());

        String transitionLabel = "\""
                + transition.getSymbol().toString()
                + "\"";
        executeScript("addEdge("+from.getNumber()+","+to.getNumber()+", "+ transitionLabel +")");
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
                if(change.getType() == ChangeType.startProductionAdded) {
                    CFProduction production = change.getNewProduction();
                    Platform.runLater(() -> {
                        String script = "insertFirstRule("
                                + "\'" + production.getLeft() + "\'"
                                + ", "
                                + "\'" + production.getRight() + "\'"
                                + ")";
                        executeScript(script);
                        executeScript("highlightRule(0)");
                    });
                }
            }
        });
    }

    private String listToJsArray(List<? extends Symbol> list) {
        String result = "[";
        ListIterator<Symbol> iter = (ListIterator<Symbol>) list.iterator();
        for(Symbol symbol = iter.next(); iter.hasNext(); symbol = iter.next()) {
            result += ('\"' + symbol.toString() + '\"');
            if(iter.hasNext())
                result += ",";
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
            String script = "setStep(";
            if (step == ParsingStep.One) script += 1;
            else if (step == ParsingStep.Two) script += 2;
            else if (step == ParsingStep.Three) script += 3;
            else if (step == ParsingStep.Results) script += 4;
            script += ")";
            executeScript(script);
        });
    }
}
