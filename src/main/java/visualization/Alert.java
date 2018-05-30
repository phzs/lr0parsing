package visualization;

import com.sun.prism.paint.Color;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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
