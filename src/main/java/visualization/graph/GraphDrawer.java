package visualization.graph;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import parsing.State;
import parsing.StateAutomaton;
import parsing.StateTransition;

public class GraphDrawer {
    public static final double paddingLeft = 25.0;
    public static final double paddingTop = 25.0;

    private WebView webView;
    private WebEngine webEngine;

    private StateAutomaton automaton;
    private double nextX = paddingLeft;
    private double nextY = paddingTop;

    public GraphDrawer(WebView targetWebView) {
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
        webEngine.load(this.getClass().getResource("/webview.html").toExternalForm());
        JSObject window = (JSObject) webEngine.executeScript("window");
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

        webEngine.executeScript("addNode("+state.getNumber()+", "+content+")");
        System.out.println("addNode("+state.getNumber()+", "+content+")");
    }

    private void drawTransition(StateTransition transition) {
        State from = this.automaton.getState(transition.getFromState());
        State to = this.automaton.getState(transition.getToState());

        String transitionLabel = "\""
                + transition.getSymbol().toString()
                + "\"";
        webEngine.executeScript("addEdge("+from.getNumber()+","+to.getNumber()+", "+ transitionLabel +")");
        System.out.println("addEdge("+from.getNumber()+","+to.getNumber()+", "+ transitionLabel +")");
    }

    /**
     * Sets webView and moves all drawn nodes to webView
     * (Nodes can only have one parent and lose the binding to their previous parent)
     * @param targetPane
     */
    public void setTargetPane(Pane targetPane) {

    }
    
    public void clearGraph() {
        webEngine.executeScript("clearGraph()");
    }
}
