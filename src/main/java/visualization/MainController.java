package visualization;

import base.CFGrammar;
import base.CFProduction;
import base.MetaSymbol;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import parsing.LR0Parser;
import visualization.grammar.GrammarTable;
import visualization.grammar.GrammarTableData;
import visualization.graph.GraphDrawer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private CFGrammar grammar;

    private int prodNum = 0;

    @FXML
    private Canvas graphCanvas;

    @FXML
    private TabPane tabPane;

    @FXML
    private ScrollPane grammarInputScrollPane;

    @FXML
    private Button startStopButton;

    private GrammarTable grammarTable = new GrammarTable(true);

    private GrammarTable grammarViewTable = new GrammarTable(false);

    private File grammarFile;

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
    private MenuItem menuOpen;

    @FXML
    private MenuItem menuSave;

    @FXML
    private MenuItem menuSaveAs;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initTable();

        loadGrammar(getExampleGrammar());

        startStopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println(getGrammar());
                grammarViewTable.getItems().clear();
                grammarViewTable.getItems().addAll(grammarTable.getItems());
                parsingGrammarScrollPane.setContent(grammarViewTable);
                parsingGrammarScrollPane.setFitToHeight(true);
                parsingGrammarScrollPane.setFitToWidth(true);

                new GraphDrawer(canvasPane, new LR0Parser().parse(grammar));
            }
        });
        clearRulesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                grammarTable.getItems().clear();
                startSymbolChoiceBox.getItems().clear();
                grammar = new CFGrammar();
                prodNum = 0;
            }
        });
        addRuleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                grammarTable.getItems().add(new GrammarTableData(prodNum++));
            }
        });
        removeRuleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(grammarTable.getItems().size() > 0) {
                    grammarTable.getItems().remove(grammarTable.getItems().size() - 1);
                    prodNum--;
                }

            }
        });

        menuOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
        });

        menuSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveGramamrToFile();
            }
        });

        menuSaveAs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File file = openFileChooser("Save Grammar as");
                setGrammarFile(file);
                saveGramamrToFile();
            }
        });
    }

    private void alert(String message) {
        Alert newAlert = new Alert(message);
        newAlert.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                alertBox.getChildren().remove(newAlert);
            }
        });
        alertBox.getChildren().add(newAlert);
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
}
