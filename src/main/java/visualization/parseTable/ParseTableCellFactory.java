package visualization.parseTable;


import base.Symbol;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import parsing.ParseTable;

public class ParseTableCellFactory<V> implements
        Callback<TableColumn.CellDataFeatures<ObservableMap<Symbol, ParseTable.TableEntry>, V>, ObservableValue<V>> {

    private final Object key;

    public ParseTableCellFactory(Object key) {
        this.key = key;
    }

    @Override
    public ObservableValue<V> call(TableColumn.CellDataFeatures<ObservableMap<Symbol, ParseTable.TableEntry>, V> features) {
        final ObservableMap map = features.getValue();
        final ObjectProperty<V> property = new SimpleObjectProperty<V>((V) map.get(key));
        map.addListener(new MapChangeListener<Object, V>() {
            public void onChanged(Change<?, ? extends V> change) {
                if (key.equals(change.getKey())) {
                    property.set((V) map.get(key));
                }
            }
        });
        return property;
    }
}