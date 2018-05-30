package visualization;

import base.CFProduction;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class GrammarTableData {
    private SimpleIntegerProperty id;
    private SimpleStringProperty left, right, arrow;

    public GrammarTableData(Integer id, String left, String right) {
        this.id = new SimpleIntegerProperty(id);
        this.left = new SimpleStringProperty(left);
        this.right = new SimpleStringProperty(right);
        this.arrow = new SimpleStringProperty("-->");
    }

    public GrammarTableData(int id, CFProduction production) {
        this(id, production.getLeft().toString(), production.getRight().toString());
    }

    public GrammarTableData(int id) {
        this(id, "", "");
    }

    public Integer getId() {
        return id.get();
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public String getLeft() {
        return left.get();
    }

    public void setLeft(String left) {
        this.left.set(left);
    }

    public SimpleStringProperty leftProperty() {
        return left;
    }

    public String getArrow() {
        return arrow.get();
    }

    public void setArrow(String arrow) {
        this.arrow.set(arrow);
    }

    public SimpleStringProperty arrowProperty() {
        return arrow;
    }

    public String getRight() {
        return right.get();
    }

    public void setRight(String right) {
        this.right.set(right);
    }

    public SimpleStringProperty rightProperty() {
        return right;
    }
}