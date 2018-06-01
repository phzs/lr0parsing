package visualization.grammar;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class GrammarTable extends TableView<GrammarTableData> {

    private boolean editable;

    public GrammarTable(boolean editable) {
        super();
        this.editable = editable;
        init();
    }

    private void init() {
        TableColumn<GrammarTableData, Integer> numberCol = new TableColumn<>("#");
        numberCol.setCellValueFactory(new PropertyValueFactory<GrammarTableData, Integer>("id"));
        numberCol.setSortable(false);
        numberCol.setEditable(false);

        TableColumn<GrammarTableData, String> leftCol = new TableColumn<>("left");
        leftCol.setCellValueFactory(new PropertyValueFactory<GrammarTableData, String>("left"));
        if(editable)
            leftCol.setCellFactory(new TextFieldCellFactory());
        leftCol.setPrefWidth(50);
        leftCol.setSortable(false);
        leftCol.setEditable(true);

        TableColumn<GrammarTableData, String> arrowCol = new TableColumn<>("arrow");
        arrowCol.setCellValueFactory(new PropertyValueFactory<GrammarTableData, String>("arrow"));
        arrowCol.setPrefWidth(30);
        arrowCol.setSortable(false);
        arrowCol.setEditable(false);

        TableColumn<GrammarTableData, String> rightCol = new TableColumn<>("right");
        rightCol.setCellValueFactory(new PropertyValueFactory<GrammarTableData, String>("right"));
        if(editable)
            rightCol.setCellFactory(new TextFieldCellFactory());
        rightCol.setPrefWidth(50);
        rightCol.setSortable(false);
        rightCol.setEditable(true);

        this.getColumns().addAll(numberCol, leftCol, arrowCol, rightCol);
    }
}
