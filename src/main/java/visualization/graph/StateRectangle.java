package visualization.graph;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import parsing.LR0Element;
import parsing.State;

import java.util.ArrayList;
import java.util.List;

public class StateRectangle extends Group {
    public static final double stateNumRectWidth = 25.0;
    public static final double stateNumRectHeight = 25.0;
    public static final double paddingLeft = 5.0;
    public static final double paddingTop = 5.0;
    public static final double lineGap = 5.0;
    public static final double characterHeight = 5.0;
    public static final double characterWidth = 2.5;

    private Point2D position;
    private State state;
    private Rectangle mainRect;
    private Rectangle stateNumRect;
    private Text stateNumberText;

    private List<Text> lR0ElementTexts;

    public StateRectangle(State state) {
        this.state = state;
        lR0ElementTexts = new ArrayList<>();

        mainRect = new Rectangle();
        mainRect.setWidth(calculateWidth());
        mainRect.setHeight(100+(20*state.getElements().size()));
        mainRect.setFill(Color.gray(0.85, 0.7));
        mainRect.setStroke(Color.BLACK);
        this.getChildren().add(mainRect);

        drawStateNumberBox();

        for(LR0Element element : state.getElements()) {
            Text elementText = new Text(element.toString());
            lR0ElementTexts.add(elementText);
            this.getChildren().add(elementText);
        }
    }

    private double calculateWidth() {
        double maxStringLength = 0;
        for(LR0Element element : state.getElements()) {
            double stringLength = new Text(element.toString()).getLayoutBounds().getWidth();
            if(stringLength > maxStringLength)
                maxStringLength = stringLength;
        }
        return maxStringLength + (2*paddingLeft) + stateNumRectWidth;
    }

    private void drawStateNumberBox() {
        stateNumberText = new Text(state.getNumber()+"");

        stateNumRect = new Rectangle(stateNumRectWidth, stateNumRectHeight);
        setStateNumPosition();
        stateNumRect.setFill(Color.TRANSPARENT);
        stateNumRect.setStroke(Color.BLACK);
        stateNumRect.setStrokeWidth(0.5);
        this.getChildren().add(stateNumRect);
        this.getChildren().add(stateNumberText);
    }

    private void setStateNumPosition() {
        stateNumRect.setX(mainRect.getX() + (mainRect.getWidth()- stateNumRectWidth));
        stateNumRect.setY(mainRect.getY() + (mainRect.getHeight()- stateNumRectHeight));
        stateNumberText.setX(stateNumRect.getX() + (stateNumRectWidth/2.0) - characterWidth);
        stateNumberText.setY(characterHeight + stateNumRect.getY() + (stateNumRectHeight /2.0));
    }

    private void setElementsPosition() {
        double textHeight = new Text("[").getLayoutBounds().getHeight();

        for(int i = 0; i < lR0ElementTexts.size(); i++) {
            Text text = lR0ElementTexts.get(i);
            text.setX(mainRect.getX() + paddingLeft);
            text.setY(text.getLayoutBounds().getHeight() + mainRect.getY() + paddingTop + (textHeight*i) + i*lineGap);
        }
    }

    public void setPosition(Point2D pos) {
        this.position = pos;
        mainRect.setX(pos.getX());
        mainRect.setY(pos.getY());
        setStateNumPosition();
        setElementsPosition();
    }

    public Point2D getPosition() {
        return position;
    }
}
