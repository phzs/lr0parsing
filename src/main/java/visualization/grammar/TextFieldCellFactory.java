package visualization.grammar;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;

public class TextFieldCellFactory
        implements Callback<TableColumn<GrammarTableData,String>,TableCell<GrammarTableData,String>> {

    enum Type {
        LeftCol,
        RightCol
    }
    private Type type;
    private GrammarTable table;

    public TextFieldCellFactory(GrammarTable table, Type type) {
        this.type = type;
        this.table = table;
    }

    @Override
    public TableCell<GrammarTableData, String> call(TableColumn<GrammarTableData, String> param) {
        TextFieldCell textFieldCell = new TextFieldCell(type);
        table.addTextField(textFieldCell.getTextField());
        return textFieldCell;
    }

    public static class TextFieldCell extends TableCell<GrammarTableData,String> {
        private GrammarTableTextField textField;
        private StringProperty boundToCurrently = null;

        public TextFieldCell(Type type) {
            String strCss;
            // Padding in Text field cell is not wanted - we want the Textfield itself to "be"
            // The cell.  Though, this is aesthetic only.  to each his own.  comment out
            // to revert back.
            strCss = "-fx-padding: 0;";
            ;

            this.setStyle(strCss);

            textField = new GrammarTableTextField();
            if(type == Type.LeftCol)
                textField.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        textField.validProperty().set(newValue.matches("^[A-Z]$"));
                    }
                });
            else if(type == Type.RightCol) {
                textField.textProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        textField.validProperty().set(newValue.matches("^[a-zA-Z]*"));
                    }
                });
                textField.setPrefWidth(10.0*textField.getText().length());
            }
            textField.prefWidthProperty().bind(textField.textProperty().length());

            //
            // Default style pulled from caspian.css. Used to play around with the inset background colors
            // ---trying to produce a text box without borders
            strCss = "" +
                    //"-fx-background-color: -fx-shadow-highlight-color, -fx-text-box-border, -fx-control-inner-background;" +
                    "-fx-background-color: -fx-control-inner-background;" +
                    //"-fx-background-insets: 0, 1, 2;" +
                    "-fx-background-insets: 0;" +
                    //"-fx-background-radius: 3, 2, 2;" +
                    "-fx-background-radius: 0;" +
                    "-fx-padding: 3 5 3 5;" +   /*Play with this value to center the text depending on cell height??*/
                    //"-fx-padding: 0 0 0 0;" +
                    "-fx-prompt-text-fill: derive(-fx-control-inner-background,-30%);" +
                    "-fx-cursor: text;" +
                    "";
            // Focused and hover states should be set in the CSS.  This is just a test
            // to see what happens when we set the style in code
            textField.focusedProperty().addListener(new ChangeListener<Boolean>() {

                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    TextField tf = (TextField)getGraphic();
                    String strStyleValid = "-fx-background-color: green, -fx-text-box-border, -fx-control-inner-background;" +
                            "-fx-background-insets: -0.4, 1, 2;" +
                            "-fx-background-radius: 3.4, 2, 2;";
                    String strStyleInvalid = "-fx-background-color: red, -fx-text-box-border, -fx-control-inner-background;" +
                            "-fx-background-insets: -0.4, 1, 2;" +
                            "-fx-background-radius: 3.4, 2, 2;";
                    String strStyleGotFocus = "-fx-background-color: purple, -fx-text-box-border, -fx-control-inner-background;" +
                            "-fx-background-insets: -0.4, 1, 2;" +
                            "-fx-background-radius: 3.4, 2, 2;";
                    String strStyleLostFocus = //"-fx-background-color: -fx-shadow-highlight-color, -fx-text-box-border, -fx-control-inner-background;" +
                            "-fx-background-color: -fx-control-inner-background;" +
                            "-fx-background-insets: -0.4, 1, 2;" +
                            "-fx-background-radius: 3.4, 2, 2;";
                    if(newValue.booleanValue()) {
                        tf.setStyle(textField.isValid() ? strStyleValid : strStyleInvalid);
                    } else
                        tf.setStyle(textField.isValid() ? strStyleLostFocus : strStyleInvalid);
                }
            });
            textField.setStyle(strCss);
            this.setGraphic(textField);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if(!empty) {
                // Show the Text Field
                this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                // Retrieve the actual String Property that should be bound to the TextField
                // If the TextField is currently bound to a different StringProperty
                // Unbind the old property and rebind to the new one
                ObservableValue<String> ov = getTableColumn().getCellObservableValue(getIndex());
                SimpleStringProperty sp = (SimpleStringProperty)ov;

                if(this.boundToCurrently==null) {
                    this.boundToCurrently = sp;
                    this.textField.textProperty().bindBidirectional(sp);
                }
                else {
                    if(this.boundToCurrently != sp) {
                        this.textField.textProperty().unbindBidirectional(this.boundToCurrently);
                        this.boundToCurrently = sp;
                        this.textField.textProperty().bindBidirectional(this.boundToCurrently);
                    }
                }
                //this.textField.setText(item);  // No longer need this!!!
            }
            else {
                this.setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }

        public GrammarTableTextField getTextField() {
            return textField;
        }

    }
}
