package visualization.grammar;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.LinkedList;
import java.util.List;

public class GrammarTable extends TableView<GrammarTableData> {

    private boolean editable;
    private List<GrammarTableTextField> textFields;

    public GrammarTable(boolean editable) {
        super();
        this.editable = editable;
        this.textFields = new LinkedList<>();
        init();
    }

    private void init() {
        this.setSelectionModel(null);
        TableColumn<GrammarTableData, Integer> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(new PropertyValueFactory<GrammarTableData, Integer>("id"));
        numberCol.setSortable(false);
        numberCol.setEditable(false);

        TableColumn<GrammarTableData, String> leftCol = new TableColumn<>("left");
        leftCol.setCellValueFactory(new PropertyValueFactory<GrammarTableData, String>("left"));
        if(editable)
            leftCol.setCellFactory(new TextFieldCellFactory(this, TextFieldCellFactory.Type.LeftCol));
        leftCol.setPrefWidth(40);
        leftCol.setSortable(false);
        leftCol.setEditable(true);

        TableColumn<GrammarTableData, String> arrowCol = new TableColumn<>("");
        arrowCol.setCellValueFactory(new PropertyValueFactory<GrammarTableData, String>("arrow"));
        arrowCol.setPrefWidth(30);
        arrowCol.setSortable(false);
        arrowCol.setEditable(false);

        TableColumn<GrammarTableData, String> rightCol = new TableColumn<>("right");
        rightCol.setCellValueFactory(new PropertyValueFactory<GrammarTableData, String>("right"));
        if(editable)
            rightCol.setCellFactory(new TextFieldCellFactory(this, TextFieldCellFactory.Type.RightCol));
        rightCol.setPrefWidth(250);
        rightCol.setSortable(false);
        rightCol.setEditable(true);

        this.getColumns().addAll(numberCol, leftCol, arrowCol, rightCol);
    }

    public void addTextField(GrammarTableTextField textField) {
        textFields.add(textField);
    }

    public boolean isValid() {
        for(GrammarTableTextField textField : textFields) {
            if(!textField.isValid()) {
                return false;
            }
        }
        return true;
    }

    public void updateTextFields() {
        for(GrammarTableTextField textField : textFields) {
            // trigger onChange to update color/validity
            String tmp = textField.getText();
            textField.setText("-");
            textField.setText(tmp);
        }
    }
}
