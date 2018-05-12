package parsing;

import base.TerminalSymbol;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateTransitionTest {
    @Test
    public void equalityTest() {
        StateTransition a = new StateTransition(0, 1,
                new TerminalSymbol('a'));
        StateTransition b = new StateTransition(0, 1,
                new TerminalSymbol('a'));
        assertTrue(a.equals(b));
        assertTrue(a.hashCode() == b.hashCode());

        Set<StateTransition> testSet = new HashSet<>();
        testSet.add(a);
        testSet.add(b);
        assertTrue(testSet.size() == 1);

        StateTransition c = new StateTransition(1,0,
                new TerminalSymbol('a'));

        assertFalse(a.equals(c));
        assertFalse(a.hashCode() == c.hashCode());
        assertFalse(b.equals(c));
        assertFalse(b.hashCode() == c.hashCode());
        testSet.add(c);
        assertTrue(testSet.size() == 2);

        StateTransition d = new StateTransition(7, 1,
                new TerminalSymbol('c'));
        assertFalse(a.equals(d));
        assertFalse(a.hashCode() == d.hashCode());
        assertFalse(b.equals(d));
        assertFalse(b.hashCode() == d.hashCode());
        testSet.add(d);
        assertTrue(testSet.size() == 3);
    }
}
