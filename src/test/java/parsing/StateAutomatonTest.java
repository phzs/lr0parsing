package parsing;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StateAutomatonTest {

    @Test
    public void registerStateTest() {
        StateAutomaton stateAutomaton = new StateAutomaton();
        Set<LR0Element> elements0 = new HashSet<>();
        elements0.add(new LR0Element('S', "A."));
        elements0.add(new LR0Element('A', ".S"));
        Integer stateNum = stateAutomaton.registerState(elements0);
        assertTrue(stateAutomaton.size() == 1);
        assertTrue(stateNum.equals(stateAutomaton.registerState(elements0)));
        assertTrue(stateAutomaton.size() == 1);

        Set<LR0Element> elements1 = new HashSet<>();
        elements1.add(new LR0Element('S', "A."));
        elements1.add(new LR0Element('A', ".S"));
        Integer stateNum1 = stateAutomaton.registerState(elements1);
        assertTrue(stateAutomaton.size() == 1);
        assertTrue(stateNum.equals(stateNum1));
        assertTrue(stateNum1.equals(stateAutomaton.registerState(elements0)));
        assertTrue(stateAutomaton.size() == 1);

        Set<LR0Element> elements2 = new HashSet<>();
        elements2.add(new LR0Element('S', "A."));
        elements2.add(new LR0Element('A', "S."));
        Integer stateNum2 = stateAutomaton.registerState(elements2);
        assertTrue(stateAutomaton.size() == 2);
        assertTrue(!stateNum.equals(stateNum2));
        assertTrue(stateNum2.equals(stateAutomaton.registerState(elements2)));
        assertTrue(stateAutomaton.size() == 2);
    }

    @Test
    public void findStateTest() {
        Set<LR0Element> elements0 = new HashSet<>();
        elements0.add(new LR0Element('S', "A."));
        elements0.add(new LR0Element('A', ".S"));

        Set<LR0Element> elements1 = new HashSet<>();
        elements1.add(new LR0Element('S', "A.a"));
        elements1.add(new LR0Element('A', ".Sx"));

        StateAutomaton stateAutomaton = new StateAutomaton();
        int expectedId = stateAutomaton.registerState(elements0);
        stateAutomaton.registerState(elements1);

        assertTrue(stateAutomaton.findState(elements0) == expectedId);
    }
}
