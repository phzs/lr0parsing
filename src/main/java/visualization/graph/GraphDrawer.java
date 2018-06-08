package visualization.graph;

import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import parsing.State;
import parsing.StateAutomaton;
import parsing.StateTransition;

import java.util.*;

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

        // draw existing states
        for(State state : stateAutomaton.getStates()) {
            drawState(state);
        }

        for(int i = 0; i < stateAutomaton.size(); i++) {
            for(StateTransition transition : stateAutomaton.getTransitionsFrom(i)) {
                drawTransition(transition);
            }
        }

        // handle future changes to stateAutomaton
        stateAutomaton.statesProperty().addListener(new MapChangeListener() {
            @Override
            public void onChanged(Change change) {
                if(change.wasAdded()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            drawState((State) change.getValueAdded());
                        }
                    });
                }
            }
        });
        stateAutomaton.transitionsProperty().addListener(new SetChangeListener() {
            @Override
            public void onChanged(Change change) {
                if(change.wasAdded()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            drawTransition((StateTransition) change.getElementAdded());
                        }
                    });
                }
            }
        });
    }

    private StateRectangle drawState(State state) {
        StateRectangle rect = new StateRectangle(state);
        Point2D pos = getFreePosition();
        rect.setPosition(pos);
        Text text = new Text(""+state.getNumber());
        text.setX(pos.getX());
        text.setY(pos.getY());
        targetPane.getChildren().add(rect);
        stateRectangles.put(state.getNumber(), rect);
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
    
    public void clearGraph() {
        for (StateRectangle rect : stateRectangles.values()) {
            targetPane.getChildren().remove(rect);
        }
        for (TransitionArrow arrow : transitionArrows) {
            targetPane.getChildren().remove(arrow);
        }
        stateRectangles.clear();
        transitionArrows.clear();
    }
}
