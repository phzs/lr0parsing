package visualization;

import analysis.Analyzer;
import base.CFGrammar;
import base.CFProduction;
import base.MetaSymbol;
import base.Symbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import parsing.ParseTable;
import parsing.StateAutomaton;
import visualization.grammar.GrammarTable;
import visualization.grammar.GrammarTableData;
import visualization.graph.GraphDrawer;
import visualization.parseTable.ParseTableView;
import visualization.stack.StackDrawer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private MainThread mainThread;

    private CFGrammar grammar;
    private int prodNum = 0;
    private GrammarTable grammarTable = new GrammarTable(true);
    private GrammarTable grammarViewTable = new GrammarTable(false);
    private File grammarFile;
    private GraphDrawer graphDrawer;
    private StackDrawer stackDrawer;
    private AppState state;
    private StateAutomaton stateAutomaton;

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
    private ScrollPane parsingGrammarScrollPane;

    @FXML
    private ScrollPane parsingGraphScrollPane;

    @FXML
    private Pane canvasPane;

    @FXML
    private Pane parsing2CanvasPane;

    @FXML
    private MenuItem menuOpen;

    @FXML
    private MenuItem menuSave;

    @FXML
    private MenuItem menuSaveAs;

    @FXML
    private ParseTableView parsing2TableView;

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

    public void addParseTableRow(ObservableMap<Symbol, ParseTable.TableEntry> valueAdded) {
        parsing2TableView.getItems().add(valueAdded);
    }

    public void stateAutomatonFinished() {
        state = AppState.AUTOMATON_GENERATED;
        Platform.runLater(() -> {
            tabPane.getSelectionModel().select(2);
        });
    }

    public void parseTableFinished() {
        state = AppState.PARSETABLE_GENERATED;
        Platform.runLater(() -> {
            tabPane.getSelectionModel().select(3);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initTable();

        loadGrammar(getExampleGrammar());

        // bind running property inversed-bidirectional to checkBox
        stepModeCheckbox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> StepController.getInstance().runningProperty().setValue(!isNowSelected));
        StepController.getInstance().runningProperty().addListener((obs, wasSelected, isNowSelected) -> stepModeCheckbox.setSelected(!isNowSelected));

        this.state = AppState.NOT_STARTED;

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Tab>() {
                    @Override
                    public void changed(ObservableValue<? extends Tab> ov, Tab t, Tab t1) {
                        if(graphDrawer != null) {
                            if (t1.getId().equals("parsing2Tab")) {
                                graphDrawer.setTargetPane(parsing2CanvasPane);
                            } else if (t1.getId().equals("parsingTab")) {
                                graphDrawer.setTargetPane(canvasPane);
                            }
                        }
                    }
                }
        );
    }

    @FXML
    private void handleStartStopButtonAction(ActionEvent actionEvent) {
        if(state == AppState.NOT_STARTED) {
            state = AppState.STARTED;
            mainThread = new MainThread(this);
            grammar = getGrammar();
            mainThread.setGrammar(grammar);
            StepController.getInstance().setMainThread(mainThread);

            grammarViewTable.getItems().clear();
            grammarViewTable.getItems().addAll(grammarTable.getItems());
            parsingGrammarScrollPane.setContent(grammarViewTable);
            parsingGrammarScrollPane.setFitToHeight(true);
            parsingGrammarScrollPane.setFitToWidth(true);
            stateAutomaton = mainThread.getStateAutomaton();

            parsing2TableView.init(grammar.getTerminalSymbols(), grammar.getMetaSymbols());

            tabPane.getSelectionModel().select(1);

            graphDrawer = new GraphDrawer(canvasPane, stateAutomaton);
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
                graphDrawer.clearGraph();
                parsing2TableView.getItems().clear();
                parsing2TableView.getColumns().clear();
                startStopButton.setText("Start Parsing");
                tabPane.getSelectionModel().select(0);
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
        StepController.getInstance().nextStep();
    }

    @FXML
    private void handleMenuOpenAction(ActionEvent actionEvent) {
        File file = openFileChooser("Open File");
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
        saveGramamrToFile();
    }

    @FXML
    private void handleMenuSaveAsAction(ActionEvent actionEvent) {
        File file = openFileChooser("Save Grammar as");
        setGrammarFile(file);
        saveGramamrToFile();
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

    private File openFileChooser(String title) {
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("Grammar (JSON)", "*.json");

        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(extFilter);
        return chooser.showOpenDialog(new Stage());
    }

    // remember file for later save actions (Menu: File -> Save)
    private void setGrammarFile(File file) {
        grammarFile = file;
        menuSave.setDisable(false);
    }

    private void saveGramamrToFile() {
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

    public GraphDrawer getGraphDrawer() {
        return graphDrawer;
    }

    public void displayAnalyzerResult(Analyzer.AnalyzerResult analyzerResult) {
        Platform.runLater(() -> {
            analysisResultDisplay.setText(analyzerResult.toString());
            analysisInputTextArea.setDisable(false);
        });
    }

    public StackDrawer getStackDrawer() {
        return stackDrawer;
    }

    public void bindAnalyzerInput(SimpleStringProperty analyzerInput) {
        Platform.runLater(() -> analysisInputDisplay.textProperty().bind(analyzerInput));
    }
}
