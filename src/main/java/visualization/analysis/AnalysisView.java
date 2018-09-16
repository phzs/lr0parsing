package visualization.analysis;

import analysis.Analyzer;
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
import parsing.StateTransition;
import visualization.JsUtil;
import visualization.StepController;
import visualization.View;
import visualization.parseTable.ParseTableCellIdentifier;
import visualization.parsing.ParsingHighlightManager;

import java.util.List;
import java.util.Set;

public class AnalysisView implements View {
    private WebView webView;
    private WebEngine webEngine;
    private ObservableStack<Character> stack;
    private ParsingHighlightManager highlightManager;

    public AnalysisView(WebView targetWebView) {
        this.webView = targetWebView;
        this.highlightManager = new ParsingHighlightManager(this);

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

    public void setAnalysisResult(String result) {
        //TODO
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
        JsUtil.initGrammar(this, grammar);
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
    }
}
