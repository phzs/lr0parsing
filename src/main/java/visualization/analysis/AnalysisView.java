package visualization.analysis;

import analysis.Analyzer;
import analysis.AnalyzerListener;
import analysis.ObservableStack;
import analysis.StackChangeListener;
import base.*;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import parsing.ParseTable;
import parsing.ParserAction;
import visualization.JsUtil;
import visualization.View;
import visualization.parseTable.ParseTableCellIdentifier;
import visualization.HighlightManager;

import java.util.List;
import java.util.Set;

public class AnalysisView implements View {
    private WebView webView;
    private WebEngine webEngine;
    private ObservableStack<Character> stack;
    private HighlightManager highlightManager;
    private CFGrammar grammar;

    public AnalysisView(WebView targetWebView) {
        this.webView = targetWebView;
        this.highlightManager = new HighlightManager(this);

        initWebView();
    }

    private void initWebView() {
        webEngine = webView.getEngine();
        //webEngine.setUserStyleSheetLocation("data:,body { font: 12px Arial; }");
        webEngine.load(this.getClass().getResource("/webview-analysis.html").toExternalForm());
        JSObject window = (JSObject) executeScript("window");
        window.setMember("app", this);
    }

    public Object executeScript(String script) {
        System.out.println("[ParsingView,"+Thread.currentThread()+"] execute: " + script);
        return webEngine.executeScript(script);
    }

    public ObservableStack<Character> getStack() {
        return stack;
    }

    public void setStack(ObservableStack<Character> stack) {
        this.stack = stack;

        stack.addListener(new StackChangeListener() {
            @Override
            public void onChanged(Change change) {
                Platform.runLater(() -> {
                    if(change.wasPushed()) {
                        executeScript("stackPush(\""+change.getValue()+"\")");
                    } else if(change.wasPopped()) {
                        executeScript("stackPop()");
                    } else if(change.wasCleared()) {
                        executeScript("stackClear()");
                    }
                });
            }
        });
    }

    public void initGrammar(CFGrammar grammar) {
        this.grammar = grammar;
        Platform.runLater(() -> {
            JsUtil.initGrammar(this, grammar);
        });
    }

    public void initParseTable(List<TerminalSymbol> terminalSymbols, List<MetaSymbol> metaSymbols) {
        JsUtil.initParseTable(this, terminalSymbols, metaSymbols);
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
                });
            }
        });
    }

    public void setAnalyzer(Analyzer analyzer) {
        analyzer.addListener(change -> {
            if(change.getType() != AnalyzerListener.ChangeType.Reduce)
                Platform.runLater(() -> {highlightManager.resetHighlightedProductions();});

            if(change.getType() == AnalyzerListener.ChangeType.Shift) {
                Platform.runLater(() -> {
                    highlightManager.resetHighlightedAnalysisInputNextSymbol();
                    highlightManager.resetParseTableHighlighting();
                    highlightManager.resetHighlightedStackItems();
                    executeScript("moveInputSymbol()");
                    setAnalysisStepDescription("Action Shift: Pushed symbol and next state id to the stack; Removed first symbol from input");
                    highlightManager.highlightStackItems(0, "highlighted-green");
                    highlightManager.highlightStackItems(1, "highlighted-green");
                });
            } else if(change.getType() == AnalyzerListener.ChangeType.Lookup) {
                Platform.runLater(() -> {
                    highlightManager.resetParseTableHighlighting();
                    highlightManager.resetHighlightedStackItems();

                    Symbol mSymbol = change.getMarkedSymbol();
                    int mState = change.getMarkedStateNum();
                    highlightManager.highlightStackItems(0, "highlighted");
                    highlightManager.highlightAnalysisInputNextSymbol("highlighted-blue");
                    highlightManager.highlightParseTableHeader(mSymbol);
                    highlightManager.highlightParseTableRow(mState);
                    highlightManager.highlightParseTableCell(new ParseTableCellIdentifier(mState, mSymbol), "highlighted-grey");

                    ParseTable.TableEntry lookupResult = change.getLookupResult();
                    String description = "Lookup the next action in the parse table. Found: \"" + lookupResult + "\" - ";
                    if(lookupResult.getAction() == ParserAction.Shift)
                        description += "The next action will be Shift";
                    else if(lookupResult.getAction() == ParserAction.Reduce)
                        description += "The next action will be Reduce";
                    else if(lookupResult.getAction() == ParserAction.Accept)
                        description += "The next action will be Accept";
                    else description += lookupResult.getAction().toString();
                    setAnalysisStepDescription(description);
                });
            } else if(change.getType() == AnalyzerListener.ChangeType.Reduce) {
                Platform.runLater(() -> {
                    highlightManager.resetParseTableHighlighting();
                    highlightManager.resetHighlightedAnalysisInputNextSymbol();
                    setAnalysisStepDescription("Action reduce n: " +
                            "<ul>" +
                            "<li class=\"text-danger\">Remove the top x elements from the stack, where" +
                            "<ul><li>x is 2 times the number of symbols on the right side of the production with number n</li></ul>" +
                            "</li>" +
                            "<li>Add the meta symbol of the production (left side) to the stack</li>" +
                            "</ul>");
                    for(int i = 0; i < change.getReducePopAmount(); i++)
                        highlightManager.highlightStackItems(i, "highlighted-red");
                    int productionNumber = change.getMarkedProduction();
                    highlightManager.highlightProduction(productionNumber);

                    String fromSequence = grammar.getProductionList().get(productionNumber).getLeft() + change.getSequence().toString();
                    executeScript("addDerivation("+productionNumber+", \""+fromSequence+"\")");
                });
            } else if(change.getType() == AnalyzerListener.ChangeType.Reduce2) {
                Platform.runLater(() -> {
                    highlightManager.highlightProduction(change.getMarkedProduction()); // should already be highlighted, just to make sure
                    highlightManager.highlightStackItems(0, "highlighted");
                    setAnalysisStepDescription("Action reduce n (part 2): " +
                            "<ul>" +
                            "<li>Added the meta symbol of the production (left side) to the stack</li>" +
                            "<li>Now looking up the entry in the parse table matching the last two stack entries</li>" +
                            "</ul>");
                });
            } else if(change.getType() == AnalyzerListener.ChangeType.ReduceLookup) {
                Platform.runLater(() -> {
                    highlightManager.resetHighlightedStackItems();

                    int mStateNum = change.getMarkedStateNum();
                    Symbol mSymbol = change.getMarkedSymbol();

                    highlightManager.highlightStackItems(0, "highlighted-blue");
                    highlightManager.highlightStackItems(1, "highlighted");

                    highlightManager.highlightParseTableRow(mStateNum);
                    highlightManager.highlightParseTableHeader(mSymbol);
                    highlightManager.highlightParseTableCell(
                            new ParseTableCellIdentifier(mStateNum, mSymbol),
                            "highlighted-green"
                    );
                    setAnalysisStepDescription("Action reduce n (part 3): " +
                            "<ul>" +
                            "<li>Looked up the entry in the parse table matching the last two stack entries - Found " +
                            change.getLookupResult().getNumber() +
                            "</li>" +
                            "<li> Adding this number to the stack" +
                            "</ul>");
                });
            } else if(change.getType() == AnalyzerListener.ChangeType.ReduceFinal) {
                Platform.runLater(() -> {
                    highlightManager.resetParseTableHighlighting();
                    highlightManager.resetHighlightedStackItems();
                    highlightManager.highlightStackItems(0, "highlighted-green");
                });
            } else if(change.getType() == AnalyzerListener.ChangeType.Accept) {
                Platform.runLater(() -> {
                    highlightManager.resetHighlightedParseTableCells();
                    highlightManager.highlightParseTableCell(
                            new ParseTableCellIdentifier(change.getMarkedStateNum(), change.getMarkedSymbol()),
                            "highlighted-green");
                    setAnalysisStepDescription("Sequence accepted");
                    executeScript("finishDerivation()");
                });
            } else if(change.getType() == AnalyzerListener.ChangeType.Error) {
                Platform.runLater(() -> {
                    highlightManager.resetHighlightedStackItems();
                    highlightManager.resetParseTableHighlighting();
                    highlightManager.resetHighlightedProductions();
                    Symbol mSymbol = change.getMarkedSymbol();
                    int mState = change.getMarkedStateNum();
                    highlightManager.highlightParseTableHeader(mSymbol);
                    highlightManager.highlightParseTableRow(mState);
                    highlightManager.highlightStackItems(0, "highlighted");
                    highlightManager.highlightAnalysisInputNextSymbol("highlighted-blue");
                    highlightManager.highlightParseTableCell(
                            new ParseTableCellIdentifier(change.getMarkedStateNum(), change.getMarkedSymbol()),
                            "highlighted-red");
                    setAnalysisStepDescription("Sequence not accepted");
                });
            }
        });
    }

    private void setAnalysisStepDescription(String text) {
        executeScript("setAnalysisStepDescription(\'"+text+"\')");
    }

    public void displayResult(Analyzer.AnalyzerResult result) {
        String resultStr = "Unknown";
        int resultMode = 1;
        if(result != null) {
            Boolean success = result.getSuccess();
            if (success != null) {
                resultStr = success ? "Sequence accepted" : "Sequence NOT accepted";
                resultMode = success ? 0 : 2;
            }
        }
        executeScript("setResult(\""+resultStr+"\", "+resultMode+")");
    }

    public void displayConflicts(Set<ParseTableCellIdentifier> cellsWithConflict) {
        executeScript("showAnalysisError()");

        for(ParseTableCellIdentifier cellIdentifier : cellsWithConflict) {
            highlightManager.highlightParseTableCell(cellIdentifier, "");
        }
    }

    public void reset() {
        executeScript("clearRules()");
        executeScript("clearParseTable()");
        executeScript("resetAnalysis() ");
        executeScript("setInput(\"\")");
    }

    public void setAnalysisInput(String input) {
        Platform.runLater(() -> {
            highlightManager.resetParseTableHighlighting();
            highlightManager.resetHighlightedAnalysisInputNextSymbol();
            highlightManager.resetHighlightedProductions();
            highlightManager.resetHighlightedStackItems();
            executeScript("resetResult()");
            executeScript("setInput(\""+input+"\")");
        });
    }
}
