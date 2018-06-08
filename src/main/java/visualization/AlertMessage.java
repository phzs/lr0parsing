package visualization;

import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class AlertMessage extends Label {

    public AlertMessage(String message) {
        this.setText(message);
        this.setStyle("-fx-background-color: red;");
        this.setWidth(400);
        this.setHeight(50);
        this.setFont(new Font(15));
    }
}
