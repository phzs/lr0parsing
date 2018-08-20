package parsing;

import base.CFGrammar;
import base.CFGrammarTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LR0ParserTest {
    @Test
    public void parseTest() {
        CFGrammar exampleGrammar = CFGrammarTest.getExampleGrammar();

        LR0Parser parser = new LR0Parser();
        StateAutomaton result = new StateAutomaton();
        parser.setStateAutomaton(result);
        parser.parse(exampleGrammar);

        // 1. check states
        List<Set<LR0Element>> expectedElementSets = new LinkedList<>();
        Set<LR0Element> state0set = new HashSet<>();
        state0set.add(new LR0Element('Z', ".S"));
        state0set.add(new LR0Element('S', ".Sb"));
        state0set.add(new LR0Element('S', ".bAa"));
        expectedElementSets.add(state0set);

        Set<LR0Element> state1set = new HashSet<>();
        state1set.add(new LR0Element('Z', "S."));
        state1set.add(new LR0Element('Z', "S.b"));
        expectedElementSets.add(state1set);

        Set<LR0Element> state2set = new HashSet<>();
        state2set.add(new LR0Element('Z', "Sb."));
        expectedElementSets.add(state2set);

        Set<LR0Element> state3set = new HashSet<>();
        state3set.add(new LR0Element('S', "a.Aa"));
        state3set.add(new LR0Element('A', ".aSc"));
        state3set.add(new LR0Element('A', ".a"));
        state3set.add(new LR0Element('A', ".aSb"));
        expectedElementSets.add(state3set);

        Set<LR0Element> state4set = new HashSet<>();
        state4set.add(new LR0Element('S', "bA.a"));
        expectedElementSets.add(state4set);

        Set<LR0Element> state5set = new HashSet<>();
        state5set.add(new LR0Element('S', "bA.a"));
        expectedElementSets.add(state5set);

        Set<LR0Element> state6set = new HashSet<>();
        state6set.add(new LR0Element('A', "a.Sc"));
        state6set.add(new LR0Element('A', "a."));
        state6set.add(new LR0Element('A', "a.Sb"));
        state6set.add(new LR0Element('S', ".Sb"));
        state6set.add(new LR0Element('S', ".bAa"));
        expectedElementSets.add(state6set);

        Set<LR0Element> state7set = new HashSet<>();
        state7set.add(new LR0Element('A', "aS.c"));
        state7set.add(new LR0Element('A', "aS.b"));
        state7set.add(new LR0Element('S', "S.b"));
        expectedElementSets.add(state7set);

        Set<LR0Element> state8set = new HashSet<>();
        state8set.add(new LR0Element('A', "a.Sc"));
        expectedElementSets.add(state8set);

        Set<LR0Element> state9set = new HashSet<>();
        state9set.add(new LR0Element('A', "aSb."));
        state9set.add(new LR0Element('S', "Sb."));
        expectedElementSets.add(state9set);

        for(Set<LR0Element> expectedSet : expectedElementSets) {
            State state0 = result.getState(result.findState(state0set));
            assertTrue(state0.getElements().containsAll(state0set),
                    expectedSet.toString()+" not found");
            assertTrue(state0.getElements().size() == state0set.size(),
                    expectedSet.toString()+" size not matching");
        }
        assertTrue(result.size() == expectedElementSets.size());
    }
}
