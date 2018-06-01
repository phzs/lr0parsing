package visualization;

import base.CFGrammar;
import base.CFProduction;
import base.MetaSymbol;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import visualization.grammar.GrammarTable;
import visualization.grammar.GrammarTableData;
import visualization.grammar.TextFieldCellFactory;

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

        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        gc.beginPath();
        gc.moveTo(30.5, 30.5);
        gc.lineTo(150.5, 30.5);
        gc.lineTo(150.5, 150.5);
        gc.lineTo(30.5, 30.5);
        gc.stroke();

        startStopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert newAlert = new Alert("this is a test");
                newAlert.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        alertBox.getChildren().remove(newAlert);
                    }
                });
                //alertBox.getChildren().add(newAlert);

                System.out.println(getGrammar());
                grammarViewTable.getItems().clear();
                grammarViewTable.getItems().addAll(grammarTable.getItems());
                parsingGrammarScrollPane.setContent(grammarViewTable);
                parsingGrammarScrollPane.setFitToHeight(true);
                parsingGrammarScrollPane.setFitToWidth(true);
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
    }
}
