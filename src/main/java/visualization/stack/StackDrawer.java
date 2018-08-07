package visualization.stack;

import analysis.ObservableStack;
import analysis.StackChangeListener;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Stack;

public class StackDrawer {

    private Pane targetPane;
    private ObservableStack<Character> stack;
    private Stack<StackItem> items;

    private double nextX = 0.0;

    public StackDrawer(Pane targetPane) {
        this.targetPane = targetPane;
        this.items = new Stack<>();
    }

    public StackDrawer(Pane targetPane, ObservableStack stack) {
        this(targetPane);
        setStack(stack);
    }

    private void push(Character character) {
        StackItem newItem = new StackItem(character, nextX);
        this.items.push(newItem);
        targetPane.getChildren().add(newItem);
        nextX += StackItem.WIDTH;
    }

    private void pop() {
        StackItem lastItem = this.items.pop();
        targetPane.getChildren().remove(lastItem);
        nextX -= StackItem.WIDTH;
    }

    private void clear() {
        this.items.clear();
        targetPane.getChildren().clear();
        nextX = 0.0;
    }

    public void setStack(ObservableStack<Character> stack) {
        this.stack = stack;
        this.stack.addListener(new StackUpdateChangeListener());
    }

    public ObservableStack<Character> getStack() {
        return stack;
    }

    private static class StackItem extends Group {

        public static final double WIDTH = 50.0;
        public static final double HEIGHT = 50.0;
        private Rectangle mainRect;
        private Text text;

        public StackItem(Character character, double xPos) {
            super();
            this.setStyle("-fx-padding: 0 10 0 0");
            this.mainRect = new Rectangle(WIDTH, HEIGHT);
            this.mainRect.setX(xPos);
            this.mainRect.setStroke(Color.BLACK);
            this.mainRect.setFill(Color.LINEN);
            this.getChildren().add(mainRect);

            this.text = new Text(Character.toString(character));
            this.text.setX(mainRect.getX() + (mainRect.getHeight()/2.0) - 5.0);
            this.text.setY(mainRect.getY() + (mainRect.getWidth()/2.0) - 5.0);
            this.getChildren().add(text);
        }

        public Rectangle getMainRect() {
            return mainRect;
        }

        public void setMainRect(Rectangle mainRect) {
            this.mainRect = mainRect;
        }

        public Text getText() {
            return text;
        }

        public void setText(Text text) {
            this.text = text;
        }
    }

    private class StackUpdateChangeListener implements StackChangeListener<Character> {

        @Override
        public void onChanged(Change change) {
            if(change.wasPushed()) {
                Platform.runLater(() -> {
                    push((Character) change.getValue());
                });
            } else if(change.wasCleared()) {
                Platform.runLater(() -> {
                    clear();
                });
            } else if(change.wasPopped()) {
                Platform.runLater(() -> {
                    pop();
                });
            }
        }
    }
}
