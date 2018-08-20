package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
    private static final String APPLICATION_TITLE = "LR0 Parsing Visualization";
    private static final double WINDOW_MIN_WIDTH = 800;
    private static final double WINDOW_MIN_HEIGHT = 600;
    private static final double WINDOW_SIZE_HORIZONTAL_PERCENT = 80;
    private static final double WINDOW_SIZE_VERTICAL_PERCENT = 80;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResource("/main.fxml"));

        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setScene(new Scene(root));
        Point2D windowBounds = getWindowBounds();
        primaryStage.setMinWidth(WINDOW_MIN_WIDTH);
        primaryStage.setMinHeight(WINDOW_MIN_HEIGHT);
        primaryStage.setWidth(windowBounds.getX());
        primaryStage.setHeight(windowBounds.getY());
        primaryStage.show();
    }

    private Point2D getWindowBounds() {
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        double spaceH = (primaryScreenBounds.getWidth() / 100.0) * (100 - WINDOW_SIZE_HORIZONTAL_PERCENT);
        double spaceV = (primaryScreenBounds.getHeight() / 100.0) * (100 - WINDOW_SIZE_VERTICAL_PERCENT);
        return new Point2D(primaryScreenBounds.getWidth() - spaceH, primaryScreenBounds.getHeight() - spaceV);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
