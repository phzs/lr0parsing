package visualization;

import analysis.Analyzer;
import analysis.ObservableStack;
import base.CFGrammar;
import base.CFGrammarListener;
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
        System.out.println("MainThread created");
    }

    @Override
    protected Void call() throws Exception {
        try {
            System.out.println("MainThread call");
            int i = 0;

            // phase 1: grammar -> stateAutomaton
            parser.setStateAutomaton(stateAutomaton);
            grammar.addListener(new CFGrammarListener() {
                /*
                    There must be another ChangeListener on the grammar here, because this thread needs to be paused
                    when the grammar changes.
                    The other one in ParseView can not do this - otherwise the GUI-Thread will freeze.
                 */
                @Override
                public void onChanged(Change change) {
                    if (change.getType() == ChangeType.startProductionAdded) {
                        StepController.getInstance().registerStep("parse:prepared", "Step 1 (adding a new start production) finished", true);
                        mainController.parsingPreparationFinished(change.getNewProduction());
                    }
                }
            });
            parser.parse(grammar);
            if(StepController.getInstance().getLastCommand() == StepController.Command.Continue)
                mainController.getParsingView().drawGraph();
            StepController.getInstance().registerStep("parse:finished", "Step 2 (building the state automaton) finished", true);
            grammar.removeAllListeners();
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
            mainController.parseTableFinished(parseTable);

            // phase 3
            analyzerStack = analyzer.getStack();
            mainController.getAnalysisView().setStack(analyzerStack);
            while (!isCancelled()) {
                StepController.getInstance().registerStep("mainThread:readyToAnalyze", "Waiting for user input to analyze", true);
                analyzer.analyze(grammar, parseTable, analyzerInput.getValue());
                mainController.displayAnalyzerResult(analyzer.getResult());
                mainController.setControlButtonsDisable(true);
            }

            System.out.println("MainThread finished");
        } catch(Throwable t) {
            System.err.println("Throwable occured in MainThread: "+t);
            t.printStackTrace();
        }
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
