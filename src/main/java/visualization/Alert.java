package visualization;

import javafx.scene.control.Label;
import javafx.scene.text.Font;

public class Alert extends Label {

    public Alert(String message) {
        this.setText(message);
        this.setStyle("-fx-background-color: red;");
        this.setWidth(400);
        this.setHeight(50);
        this.setFont(new Font(15));
    }
}
