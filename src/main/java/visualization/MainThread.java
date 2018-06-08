package visualization;

import analysis.Analyzer;
import base.CFGrammar;
import javafx.concurrent.Task;
import parsing.LR0Parser;
import parsing.StateAutomaton;

public class MainThread extends Task<Void> {

    private LR0Parser parser;
    private Analyzer analyzer;

    // input
    private CFGrammar grammar;

    // output data structures
    private StateAutomaton stateAutomaton;

    // for output
    private MainController mainController;

    public MainThread(MainController mainController) {
        super();
        this.mainController = mainController;
        parser = new LR0Parser();
        analyzer = new Analyzer();
        stateAutomaton = new StateAutomaton();
        System.out.println("MainThread created");
    }

    @Override
    protected Void call() throws Exception {
        System.out.println("MainThread call");
        int i = 0;

        // phase 1: grammar -> stateAutomaton
        parser.setStateAutomaton(stateAutomaton);
        parser.parse(grammar);
        mainController.stateAutomatonFinished();
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
}
