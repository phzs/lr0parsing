package visualization;

import analysis.Analyzer;
import base.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import parsing.LR0Element;
import parsing.ParseTable;
import parsing.StateAutomaton;
import visualization.analysis.AnalysisView;
import visualization.grammar.GrammarTable;
import visualization.grammar.GrammarTableData;
import visualization.parsing.ParsingStep;
import visualization.parsing.ParsingView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private MainThread mainThread;

    private CFGrammar grammar;
    private int prodNum = 0;
    private GrammarTable grammarTable = new GrammarTable(true);
    private File grammarFile;
    private ParsingView parsingView;
    private AnalysisView analysisView;
    private AppState state;
    private StateAutomaton stateAutomaton;

    private WebEngine parsingWebEngine;

    @FXML
    private TabPane tabPane;

    @FXML
    private ScrollPane grammarInputScrollPane;

    @FXML
    private Button startStopButton;

    @FXML
    private Button clearRulesButton;

    @FXML
    private Button addRuleButton;

    @FXML
    private Button removeRuleButton;

    @FXML
    private ScrollPane parsingGraphScrollPane;

    @FXML
    private WebView parsingWebView;

    @FXML
    private WebView analysisWebView;

    @FXML
    private MenuItem menuOpen;

    @FXML
    private MenuItem menuSave;

    @FXML
    private MenuItem menuSaveAs;

    @FXML
    private TextField analysisInputTextField;

    @FXML
    private Button analysisStartButton;

    @FXML
    private CheckBox stepModeCheckbox;

    @FXML
    private ScrollPane parsingParent;

    @FXML
    private ScrollPane analysisParent;

    @FXML
    private Label stepControllerLabel;

    @FXML
    private HBox controlButtonBar;

    @FXML
    private Button continueButton;

    @FXML
    private Button nextStepButton;

    @FXML
    private Button previousStepButton;

    public static CFGrammar getExampleGrammar() {
        CFGrammar exampleGrammar = new CFGrammar('S');
        exampleGrammar.addProduction(new CFProduction('S', "Sb"));
        exampleGrammar.addProduction(new CFProduction('S', "bAa"));
        exampleGrammar.addProduction(new CFProduction('A', "aSc"));
        exampleGrammar.addProduction(new CFProduction('A', "a"));
        exampleGrammar.addProduction(new CFProduction('A', "aSb"));
        return exampleGrammar;
    }

    private void initTable() {
        grammarInputScrollPane.setFitToHeight(true);
        grammarInputScrollPane.setFitToWidth(true);
        grammarInputScrollPane.setContent(grammarTable);
    }

    public void addProduction(CFProduction production) {
        grammarTable.getItems().add(new GrammarTableData(prodNum++, production));
    }

    public void loadGrammar(CFGrammar grammar) {
        this.grammar = grammar;
        clearProductions();
        for(CFProduction production : grammar.getProductionList()) {
            addProduction(production);
        }
        grammarTable.updateTextFields();
        prodNum = this.grammar.getProductionList().size();
        if(prodNum > 0)
            startStopButton.setDisable(false);
    }

    public CFGrammar getGrammar() throws IllegalArgumentException {
        grammar = new CFGrammar();
        for(Object dataRow : grammarTable.getItems()) {
            GrammarTableData grammarTableData = (GrammarTableData) dataRow;
            if(grammarTableData.getLeft().length() == 0)
                throw new IllegalArgumentException();
            CFProduction newProduction = new CFProduction(
                    grammarTableData.getLeft().charAt(0),
                    grammarTableData.getRight()
                    );
            grammar.addProduction(newProduction);
        }

        // set start symbol
        grammar.setStartSymbol(grammar.getProductionList().get(0).getLeft());
        return grammar;
    }

    private void clearProductions() {
        grammarTable.getItems().clear();
        prodNum = 0;
    }

    public void addParseTableRow(int stateId, ObservableMap<Symbol, ParseTable.TableEntry> valueAdded) {
        System.out.println("MainController.addParseTableRow("+stateId+","+valueAdded+")");
        parsingView.addParseTableEntryListener(stateId, valueAdded);
        analysisView.addParseTableEntryListener(stateId, valueAdded);
    }

    public void parsingPreparationFinished(CFProduction newStartingProduction) {
        List<TerminalSymbol> terminalSymbols = grammar.getTerminalSymbols();
        List<MetaSymbol> metaSymbols = grammar.getMetaSymbols();
        terminalSymbols.add(new TerminalSymbol('$'));
        parsingView.initParseTable(terminalSymbols, metaSymbols);
        analysisView.initParseTable(terminalSymbols, metaSymbols);
        parsingView.setVisibleParsingStep(ParsingStep.Two);
        parsingView.setAcceptingElement(new LR0Element(newStartingProduction, newStartingProduction.getRight().getLength()));
        analysisView.initGrammar(grammar);
    }

    public void stateAutomatonFinished() {
        state = AppState.AUTOMATON_GENERATED;
        parsingView.setVisibleParsingStep(ParsingStep.Three);
    }

    public void parseTableFinished(ParseTable parseTable) {
        state = AppState.PARSETABLE_GENERATED;
        parsingView.setVisibleParsingStep(ParsingStep.Results);
        StepController.getInstance().registerStep("parse:finished", "Parsing finished", true);
        Platform.runLater(() -> {
            tabPane.getSelectionModel().select(2);
            if(parseTable.hasConflicts()) {
                analysisStartButton.setDisable(true);
                setAnalysisInputDisabled(true);
                analysisView.displayConflicts(parseTable.getCellsWithConflicts());
            } else
                analysisStartButton.requestFocus();

            setControlButtonsDisable(true);
        });
    }

    private void setAnalysisInputDisabled(boolean disable) {
        analysisInputTextField.setDisable(disable);
    }

    public void setControlButtonsDisable(boolean disable) {
        continueButton.setDisable(disable);
        nextStepButton.setDisable(disable);
        previousStepButton.setDisable(disable);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StepController.getInstance().setMainController(this);

        // initializing the webviews needs some time, therefore do it in advance
        parsingView = new ParsingView(parsingWebView);
        analysisView = new AnalysisView(analysisWebView);
        parsingWebEngine = parsingWebView.getEngine();

        initTable();

        loadGrammar(getExampleGrammar());

        // bind running property inversed-bidirectional to checkBox
        stepModeCheckbox.selectedProperty().addListener(
                (obs, wasSelected, isNowSelected)
                        -> StepController.getInstance().runningProperty().setValue(!isNowSelected)
        );
        StepController.getInstance().runningProperty().addListener(
                (obs, wasSelected, isNowSelected)
                        -> stepModeCheckbox.setSelected(!isNowSelected)
        );

        this.state = AppState.NOT_STARTED;

        controlButtonBar.setVisible(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
                        controlButtonBar.setVisible(! t1.getId().equals("inputTab") );
                    }
                }
        );

        parsingParent.widthProperty().addListener((observable, oldValue, newValue) -> {
            parsingWebView.setPrefWidth((Double) newValue);
        });
        parsingParent.heightProperty().addListener((observable, oldValue, newValue) -> {
            parsingWebView.setPrefHeight((Double) newValue);
        });
        analysisParent.widthProperty().addListener((observable, oldValue, newValue) -> {
            analysisWebView.setPrefWidth((Double) newValue);
        });
        analysisParent.heightProperty().addListener((observable, oldValue, newValue) -> {
            analysisWebView.setPrefHeight((Double) newValue);
        });
    }

    private void setGrammarWritable(boolean writable) {
        grammarTable.setDisable(!writable);
        menuOpen.setDisable(!writable);
    }

    private void startProgram(CFGrammar grammar) {
        this.grammar = grammar;
        state = AppState.STARTED;
        mainThread = new MainThread(this);
        mainThread.setGrammar(grammar);
        StepController.getInstance().setMainThread(mainThread);

        stateAutomaton = mainThread.getStateAutomaton();
        parsingView.setStateAutomaton(stateAutomaton);


        parsingView.initGrammar(grammar);

        analysisView.setAnalyzer(mainThread.getAnalyzer());

        parsingParent.setVvalue(0); // scroll to top
        tabPane.getSelectionModel().select(1);

        StepController.getInstance().start();

        setControlButtonsDisable(false);
        analysisStartButton.setDisable(false);
        setAnalysisInputDisabled(false);
        continueButton.requestFocus();
    }

    @FXML
    private void handleStartButtonAction(ActionEvent actionEvent) {
        boolean validGrammar = true;
        try {
            CFGrammar grammar = getGrammar();
        } catch(IllegalArgumentException e) {
            validGrammar = false;
            System.err.println("Not accepting grammar because of left with length 0");
        }
        if(!grammar.validate() || !validGrammar) {
            errorDialog("Invalid input", "Please only insert valid grammar productions",
                    "- The left side must contain exactly one meta symbol (uppercase letter).\n" +
                            "- The right side may contain any number of meta symbols (uppercase letters) or terminal symbols (lowercase letters).\n" +
                            "- Numbers, whitespaces or special characters are not allowed.");
            grammarTable.updateTextFields();
        } else {
            if (state == AppState.NOT_STARTED) {
                startProgram(grammar);
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Restart Parsing");
                alert.setHeaderText("All results will be lost if you choose to restart parsing");
                alert.setContentText("Do you want to restart the parsing progress?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    StepController.getInstance().killMainThread();
                    parsingView.reset();
                    analysisView.reset();
                    analysisInputTextField.setText("");
                    StepController.getInstance().clearSteps();
                    state = AppState.NOT_STARTED;
                    startProgram(grammar);
                }
            }
        }
    }



    @FXML
    private void handleNextStepButton(ActionEvent actionEvent) {
        StepController.getInstance().nextStep();
    }

    @FXML
    private void handlePreviousStepButton(ActionEvent actionEvent) {
        StepController.getInstance().previousStep();
    }

    @FXML
    public void handleContinueButton(ActionEvent actionEvent) {
        parsingView.cleanForContinue();
        StepController.getInstance().doContinue();
    }

    @FXML
    private void handleClearRulesButtonAction(ActionEvent actionEvent) {
        clearProductions();
        grammar = new CFGrammar();
        startStopButton.setDisable(true);
    }

    @FXML
    private void handleAddRuleButtonAction(ActionEvent actionEvent) {
        grammarTable.getItems().add(new GrammarTableData(prodNum++));
        if(prodNum > 0)
            startStopButton.setDisable(false);
    }

    @FXML
    private void handleRemoveRuleButtonAction(ActionEvent actionEvent) {
        if(grammarTable.getItems().size() > 0) {
            grammarTable.getItems().remove(grammarTable.getItems().size() - 1);
            prodNum--;
            if(prodNum < 1)
                startStopButton.setDisable(true);
        }
    }

    @FXML
    private void handleAnalysisStartButtonAction(ActionEvent actionEvent) {
        String input = analysisInputTextField.getText().replace("\n", "");
        if(!input.matches("[a-z]*")) {
            errorDialog("Invalid input", "The input may only contain valid terminal symbols", "");
        } else {
            mainThread.pushNextAnalyzerInput(input);
            setAnalysisInputDisabled(true);
            analysisStartButton.setDisable(true);
            setControlButtonsDisable(false);
            StepController.getInstance().nextStep();

            continueButton.requestFocus();
        }
    }

    @FXML
    private void handleMenuOpenAction(ActionEvent actionEvent) {
        File file = getFileChooser("Open Grammar File").showOpenDialog(new Stage());
        setGrammarFile(file);
        tabPane.getSelectionModel().select(0);
        if(file == null)
            return;
        try {
            grammar = CFGrammar.fromFile(file);
            loadGrammar(grammar);
        } catch (IOException e) {
            errorDialog("Error","Unable to load a grammar from the specified file.", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMenuSaveAction(ActionEvent actionEvent) {
        saveGrammarToFile();
    }

    @FXML
    private void handleMenuSaveAsAction(ActionEvent actionEvent) {
        File file = getFileChooser("Save Grammar as").showSaveDialog(new Stage());
        setGrammarFile(file);
        saveGrammarToFile();
    }

    @FXML
    public void handleMenuQuitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    private void errorDialog(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private FileChooser getFileChooser(String title) {
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Grammar (JSON)", "*.json");

        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(extFilter);
        return chooser;
    }

    // remember file for later save actions (Menu: File -> Save)
    private void setGrammarFile(File file) {
        grammarFile = file;
        menuSave.setDisable(false);
    }

    private void saveGrammarToFile() {
        if(grammarFile != null) {
            try {
                FileUtils.writeStringToFile(grammarFile, getGrammar().toJSON());
            } catch (JsonProcessingException e) {
                errorDialog("Error","Unable to save the grammar to the specified location.", e.getLocalizedMessage());
                e.printStackTrace();
            } catch (IOException e) {
                errorDialog("Error","Unable to save the grammar to the specified location.", e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public ParsingView getParsingView() {
        return parsingView;
    }

    public void displayAnalyzerResult(Analyzer.AnalyzerResult analyzerResult) {
        Platform.runLater(() -> {
            analysisInputTextField.setDisable(false);
            analysisStartButton.setDisable(false);
            analysisView.displayResult(analyzerResult);
        });
    }

    public AnalysisView getAnalysisView() {
        return analysisView;
    }


    public void displayStep(String id, String description, int repetition) {
        Platform.runLater(() -> {
            String text = description;
            if(repetition > 0)
                text += " (" + repetition + ")";
            stepControllerLabel.setText(text);
        });
    }

    public void setFocusToContinue() {
        continueButton.requestFocus();
    }
}
