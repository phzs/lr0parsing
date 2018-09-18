package visualization.grammar;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextField;

public class GrammarTableTextField extends TextField {
    private SimpleBooleanProperty valid = new SimpleBooleanProperty(true);

    public boolean isValid() {
        return valid.getValue();
    }

    public SimpleBooleanProperty validProperty() {
        return valid;
    }
}
