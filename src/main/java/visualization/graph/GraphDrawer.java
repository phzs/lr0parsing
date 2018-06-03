package visualization.graph;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import parsing.State;
import parsing.StateAutomaton;
import parsing.StateTransition;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GraphDrawer {
    public static final double paddingLeft = 25.0;
    public static final double paddingTop = 25.0;

    private Pane targetPane;
    private StateAutomaton automaton;
    private double nextX = paddingLeft;
    private double nextY = paddingTop;

    private Map<Integer, StateRectangle> stateRectangles;
    private List<TransitionArrow> transitionArrows;

    public GraphDrawer(Pane targetPane, StateAutomaton stateAutomaton) {
        this.targetPane = targetPane;
        this.automaton = stateAutomaton;
        this.stateRectangles = new HashMap<>();
        this.transitionArrows = new LinkedList<>();

        for(State state : stateAutomaton.getStates()) {
            stateRectangles.put(state.getNumber(), drawState(state));
        }

        for(int i = 0; i < stateAutomaton.size(); i++) {
            for(StateTransition transition : stateAutomaton.getTransitionsFrom(i)) {
                drawTransition(transition);
            }
        }
    }

    private StateRectangle drawState(State state) {
        StateRectangle rect = new StateRectangle(state);
        Point2D pos = getFreePosition();
        rect.setPosition(pos);
        Text text = new Text(""+state.getNumber());
        text.setX(pos.getX());
        text.setY(pos.getY());
        targetPane.getChildren().add(rect);
        return rect;
    }

    private void drawTransition(StateTransition transition) {
        State from = this.automaton.getState(transition.getFromState());
        State to = this.automaton.getState(transition.getToState());
        StateRectangle fromRect = stateRectangles.get(from.getNumber());
        StateRectangle toRect = stateRectangles.get(to.getNumber());

        TransitionArrow newArrow = new TransitionArrow(fromRect, toRect);
        targetPane.getChildren().add(newArrow);
        transitionArrows.add(newArrow);
    }

    private Point2D getFreePosition() {
        Point2D result = new Point2D(nextX, nextY);
        if(nextX +250 < 800)
            nextX += 300;
        else {
            nextX = paddingLeft;
            nextY += 400;
        }
        return result;
    }

    /**
     * Sets targetPane and moves all drawn nodes to targetPane
     * (Nodes can only have one parent and lose the binding to their previous parent)
     * @param targetPane
     */
    public void setTargetPane(Pane targetPane) {
        if(targetPane != this.targetPane) {
            for (StateRectangle rect : stateRectangles.values()) {
                targetPane.getChildren().add(rect);
            }
            for (TransitionArrow arrow : transitionArrows) {
                targetPane.getChildren().add(arrow);
            }
            this.targetPane = targetPane;
        }
    }
}
