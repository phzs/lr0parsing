package visualization.graph;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;


public class TransitionArrow extends Group {
    private Line arrowLine;
    private Polygon arrowHead;

    public TransitionArrow(StateRectangle from, StateRectangle to) {
        arrowLine = new Line();
        arrowLine.setStroke(Color.BLACK);
        Point2D fromPosition = from.getPosition();
        arrowLine.setStartX(fromPosition.getX());
        arrowLine.setStartY(fromPosition.getY());
        Point2D toPosition = to.getPosition();
        arrowLine.setEndX(toPosition.getX());
        arrowLine.setEndY(toPosition.getY());

        this.getChildren().add(arrowLine);

        arrowHead = new Polygon();
        arrowHead.getPoints().add(arrowLine.getEndX());
        arrowHead.getPoints().add(arrowLine.getEndY());

    }
}
