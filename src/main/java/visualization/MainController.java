package visualization;

import analysis.Analyzer;
import base.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import parsing.ParseTable;
import parsing.StateAutomaton;
import visualization.grammar.GrammarTable;
import visualization.grammar.GrammarTableData;
import visualization.parseTable.ParseTableView;
import visualization.parsing.ParsingStep;
import visualization.parsing.ParsingView;
import visualization.stack.StackDrawer;

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
    private StackDrawer stackDrawer;
    private AppState state;
    private StateAutomaton stateAutomaton;

    private WebEngine webEngine;

    @FXML
    private Pane stackPane;

    @FXML
    private TabPane tabPane;

    @FXML
    private ScrollPane grammarInputScrollPane;

    @FXML
    private Button startStopButton;

    @FXML
    private ChoiceBox startSymbolChoiceBox;

    @FXML
    private VBox alertBox;

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
    private MenuItem menuOpen;

    @FXML
    private MenuItem menuSave;

    @FXML
    private MenuItem menuSaveAs;

    @FXML
    private ParseTableView analysisTableView;

    @FXML
    private Label analysisInputDisplay;

    @FXML
    private Label analysisResultDisplay;

    @FXML
    private TextArea analysisInputTextArea;

    @FXML
    private Button analysisStartButton;

    @FXML
    private CheckBox stepModeCheckbox;

    @FXML
    private ScrollPane parsingParent;

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
        //TableColumn<CFProduction, String>
        grammarTable.getItems().add(new GrammarTableData(prodNum++, production));

        char left = production.getLeft().getRepresentation();
        if(!startSymbolChoiceBox.getItems().contains(left))
        startSymbolChoiceBox.getItems().add(left);
        if(startSymbolChoiceBox.getValue() == null)
            startSymbolChoiceBox.setValue(left);
    }

    public void loadGrammar(CFGrammar grammar) {
        this.grammar = grammar;
        clearProductions();
        for(CFProduction production : grammar.getProductionList()) {
            addProduction(production);
        }
        startSymbolChoiceBox.setValue(grammar.getStartSymbol().getRepresentation());
    }

    public CFGrammar getGrammar() {
        grammar = new CFGrammar();
        if(startSymbolChoiceBox.getValue() != null)
            grammar.setStartSymbol(new MetaSymbol((char) startSymbolChoiceBox.getValue()));
        for(Object dataRow : grammarTable.getItems()) {
            GrammarTableData grammarTableData = (GrammarTableData) dataRow;
            CFProduction newProduction = new CFProduction(
                    grammarTableData.getLeft().charAt(0),
                    grammarTableData.getRight()
                    );
            grammar.addProduction(newProduction);
        }
        return grammar;
    }

    private void clearProductions() {
        grammarTable.getItems().clear();
    }

    public void addParseTableRow(int stateId, ObservableMap<Symbol, ParseTable.TableEntry> valueAdded) {
        System.out.println("MainController.addParseTableRow("+stateId+","+valueAdded+")");
        parsingView.addParseTableEntryListener(stateId, valueAdded);
        analysisTableView.getItems().add(valueAdded);
    }

    public void parsingPreparationFinished() {
        parsingView.setVisibleParsingStep(ParsingStep.Two);
    }

    public void stateAutomatonFinished() {
        state = AppState.AUTOMATON_GENERATED;
        parsingView.setVisibleParsingStep(ParsingStep.Three);
    }

    public void parseTableFinished() {
        state = AppState.PARSETABLE_GENERATED;
        Platform.runLater(() -> {
            parsingView.setVisibleParsingStep(ParsingStep.Results);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // initializing the webview needs some time, therefore do it in advance
        parsingView = new ParsingView(parsingWebView);
        webEngine = parsingWebView.getEngine();

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

        /*
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
                        if (graphDrawer != null) {
                            if (t1.getId().equals("parsing2Tab")) {
                                graphDrawer.setTargetPane(parsing2CanvasPane);
                            } else if (t1.getId().equals("parsingTab")) {
                                //graphDrawer.setTargetPane(canvasPane);
                            }
                        }
                    }
                }
        );
        */
        parsingParent.widthProperty().addListener((observable, oldValue, newValue) -> {
            parsingWebView.setPrefWidth((Double) newValue);
        });
        parsingParent.heightProperty().addListener((observable, oldValue, newValue) -> {
            parsingWebView.setPrefHeight((Double) newValue);
        });
    }

    private void setGrammarWritable(boolean writable) {
        grammarTable.setDisable(!writable);
        menuOpen.setDisable(!writable);
    }

    @FXML
    private void handleStartStopButtonAction(ActionEvent actionEvent) {
        if(state == AppState.NOT_STARTED) {
            state = AppState.STARTED;
            mainThread = new MainThread(this);
            grammar = getGrammar();
            mainThread.setGrammar(grammar);
            StepController.getInstance().setMainThread(mainThread);
            setGrammarWritable(false);

            stateAutomaton = mainThread.getStateAutomaton();

            List<TerminalSymbol> terminalSymbols = grammar.getTerminalSymbols();
            List<MetaSymbol> metaSymbols = grammar.getMetaSymbols();
            parsingView.initGrammar(grammar);

            parsingView.initParseTable(terminalSymbols, metaSymbols);
            analysisTableView.init(terminalSymbols, metaSymbols);

            tabPane.getSelectionModel().select(1);

            parsingView.setStateAutomaton(stateAutomaton);

            stackDrawer = new StackDrawer(stackPane);
            StepController.getInstance().start();
            startStopButton.setText("Reset to Start");
        } else {
            mainThread.cancel();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reset Confirmation");
            alert.setHeaderText("All results will be lost if you choose to reset parsing");
            alert.setContentText("Do you want to reset the parsing progress?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                parsingView.clearGraph();
                startStopButton.setText("Start Parsing");
                tabPane.getSelectionModel().select(0);
                setGrammarWritable(true);
            }
            state = AppState.NOT_STARTED;
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
    private void handleClearRulesButtonAction(ActionEvent actionEvent) {
        grammarTable.getItems().clear();
        startSymbolChoiceBox.getItems().clear();
        grammar = new CFGrammar();
        prodNum = 0;
    }

    @FXML
    private void handleAddRuleButtonAction(ActionEvent actionEvent) {
        grammarTable.getItems().add(new GrammarTableData(prodNum++));
    }

    @FXML
    private void handleRemoveRuleButtonAction(ActionEvent actionEvent) {
        if(grammarTable.getItems().size() > 0) {
            grammarTable.getItems().remove(grammarTable.getItems().size() - 1);
            prodNum--;
        }
    }

    @FXML
    private void handleAnalysisStartButtonAction(ActionEvent actionEvent) {
        stackDrawer.getStack().clear();
        String input = analysisInputTextArea.getText().replace("\n", "");
        mainThread.pushNextAnalyzerInput(input);
        analysisInputTextArea.setDisable(true);
        analysisStartButton.setDisable(true);
        StepController.getInstance().nextStep();
    }

    @FXML
    private void handleMenuOpenAction(ActionEvent actionEvent) {
        File file = getFileChooser("Open Grammar File").showOpenDialog(new Stage());
        setGrammarFile(file);

        try {
            grammar = CFGrammar.fromFile(file);
            loadGrammar(grammar);
        } catch (IOException e) {
            alert("Error! File could not be opened: " + e.getLocalizedMessage());
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

    private void alert(String message) {
        AlertMessage newAlertMessage = new AlertMessage(message);
        newAlertMessage.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                alertBox.getChildren().remove(newAlertMessage);
            }
        });
        alertBox.getChildren().add(newAlertMessage);
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
                alert("Could not save grammar: " + e.getLocalizedMessage());
                e.printStackTrace();
            } catch (IOException e) {
                alert("Could not save grammar: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public ParsingView getParsingView() {
        return parsingView;
    }

    public void displayAnalyzerResult(Analyzer.AnalyzerResult analyzerResult) {
        Platform.runLater(() -> {
            analysisResultDisplay.setText(analyzerResult.toString());
            analysisInputTextArea.setDisable(false);
            analysisStartButton.setDisable(false);
        });
    }

    public StackDrawer getStackDrawer() {
        return stackDrawer;
    }

    public void bindAnalyzerInput(SimpleStringProperty analyzerInput) {
        Platform.runLater(() -> analysisInputDisplay.textProperty().bind(analyzerInput));
    }
}
