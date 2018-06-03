package visualization.parseTable;

import base.MetaSymbol;
import base.Symbol;
import base.TerminalSymbol;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import parsing.ParseTable;

import java.util.List;

public class ParseTableView extends TableView<ObservableMap<Symbol, ParseTable.TableEntry>> {

    private List<TerminalSymbol> terminalSymbols;
    private List<MetaSymbol> metaSymbols;

    public ParseTableView() {
        super();
    }

    public void init(List<TerminalSymbol> terminalSymbols, List<MetaSymbol> metaSymbols) {
        this.terminalSymbols = terminalSymbols;
        this.terminalSymbols.add(new TerminalSymbol('$'));
        this.metaSymbols = metaSymbols;

        TableColumn<ObservableMap<Symbol, ParseTable.TableEntry>, Integer> numberColumn = new TableColumn<>();
        numberColumn.setCellFactory(col -> {
            TableCell<ObservableMap<Symbol, ParseTable.TableEntry>, Integer> cell = new TableCell<>();
            cell.textProperty().bind(Bindings.createStringBinding(() -> {
                if (cell.isEmpty()) {
                    return null ;
                } else {
                    return Integer.toString(cell.getIndex());
                }
            }, cell.emptyProperty(), cell.indexProperty()));
            return cell ;
        });

        TableColumn<ObservableMap<Symbol, ParseTable.TableEntry>, String> actionTable = new TableColumn<>("Action Table");
        for(TerminalSymbol symbol : terminalSymbols) {
            char terminalSymbolRepr = symbol.getRepresentation();
            TableColumn<ObservableMap<Symbol, ParseTable.TableEntry>, String> actionColumn = new TableColumn<ObservableMap<Symbol, ParseTable.TableEntry>, String>(""+terminalSymbolRepr);
            actionColumn.setCellValueFactory(new ParseTableCellFactory<String>(symbol));
            actionTable.getColumns().add(actionColumn);
        }
        TableColumn<ObservableMap<Symbol, ParseTable.TableEntry>, String> jumpTable = new TableColumn<>("Action Table");
        for(MetaSymbol symbol : metaSymbols) {
            char metaSymbolRepr = symbol.getRepresentation();
            TableColumn<ObservableMap<Symbol, ParseTable.TableEntry>, String> jumpColumn = new TableColumn<ObservableMap<Symbol, ParseTable.TableEntry>, String>(""+metaSymbolRepr);
            jumpColumn.setCellValueFactory(new ParseTableCellFactory<String>(symbol));
            jumpTable.getColumns().add(jumpColumn);
        }
        this.getColumns().addAll(numberColumn, actionTable, jumpTable);

        // make columns resize themselves to automatically fit their content
        this.setColumnResizePolicy((param) -> true );

        this.getItems().addListener(new ListChangeListener<ObservableMap<Symbol, ParseTable.TableEntry>>() {
            @Override
            public void onChanged(Change<? extends ObservableMap<Symbol, ParseTable.TableEntry>> c) {
                double newPrefW = numberColumn.getWidth();
                for(TableColumn<ObservableMap<Symbol, ParseTable.TableEntry>, ?> col : actionTable.getColumns()) {
                    newPrefW += col.getPrefWidth();
                }
                for(TableColumn<ObservableMap<Symbol, ParseTable.TableEntry>, ?> col : jumpTable.getColumns()) {
                    newPrefW += col.getPrefWidth();
                }
                double newPrefH = 30 * getItems().size();
                setPrefHeight(newPrefH);
                setPrefWidth(newPrefW);
            }
        });
    }
}
