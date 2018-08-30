package visualization;

import analysis.Analyzer;
import analysis.ObservableStack;
import base.CFGrammar;
import base.Symbol;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import parsing.LR0Parser;
import parsing.ParseTable;
import parsing.StateAutomaton;

public class MainThread extends Task<Void> {

    private final static int SLEEP_BETWEEN_PHASES = 0;

    private LR0Parser parser;
    private Analyzer analyzer;

    // input
    private CFGrammar grammar;

    // output data structures
    private StateAutomaton stateAutomaton;
    private ParseTable parseTable;
    private SimpleStringProperty analyzerInput;
    private ObservableStack<Character> analyzerStack;
    private Analyzer.AnalyzerResult analyzerResult;

    // for output
    private MainController mainController;

    public MainThread(MainController mainController) {
        super();
        this.mainController = mainController;
        parser = new LR0Parser();
        analyzer = new Analyzer();
        stateAutomaton = new StateAutomaton();
        parseTable = new ParseTable();
        analyzerInput = new SimpleStringProperty();
        analyzerStack = new ObservableStack<>();
        analyzerResult = new Analyzer.AnalyzerResult();
        System.out.println("MainThread created");
    }

    @Override
    protected Void call() throws Exception {
        System.out.println("MainThread call");
        int i = 0;

        // phase 1: grammar -> stateAutomaton
        parser.setStateAutomaton(stateAutomaton);
        parser.parse(grammar);
        if(StepController.getInstance().isRunning())
            Thread.sleep(SLEEP_BETWEEN_PHASES);
        mainController.stateAutomatonFinished();

        // phase 2: stateAutomaton -> parseTable
        parser.setParseTable(parseTable);
        parseTable.addChangeListener(new MapChangeListener<Integer, ObservableMap<Symbol, ParseTable.TableEntry>>() {
            @Override
            public void onChanged(Change<? extends Integer, ? extends ObservableMap<Symbol, ParseTable.TableEntry>> change) {
                    mainController.addParseTableRow(change.getKey(), change.getValueAdded());
            }
        });
        parser.generateTable(grammar, stateAutomaton);
        if(StepController.getInstance().isRunning())
            Thread.sleep(SLEEP_BETWEEN_PHASES);
        mainController.parseTableFinished();
        analyzerStack = analyzer.getStack();
        mainController.getStackDrawer().setStack(analyzerStack);
        analyzer.setResult(analyzerResult);
        mainController.bindAnalyzerInput(analyzerInput);
        while(!isCancelled()) {
            StepController.getInstance().stop();
            StepController.getInstance().registerStep("mainThread:readyToAnalyze", "Waiting for user input to analyze");
            analyzer.analyze(grammar, parseTable, analyzerInput.getValue());
            mainController.displayAnalyzerResult(analyzerResult);
        }

        System.out.println("MainThread finished");
        return null;
    }

    public CFGrammar getGrammar() {
        return grammar;
    }

    public void setGrammar(CFGrammar grammar) {
        this.grammar = grammar;
    }

    public StateAutomaton getStateAutomaton() {
        return stateAutomaton;
    }

    public void pushNextAnalyzerInput(String input) {
        this.analyzerInput.setValue(input);
    }
}
