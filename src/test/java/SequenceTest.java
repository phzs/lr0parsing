import org.junit.jupiter.api.Test;
import parsing.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SequenceTest {
    @Test
    public void leftmostDerivationTest() {
        Sequence s = new Sequence("xSbax");
        assertTrue(s.applyLeftmostDerivation(new CFProduction('S', "cba")));
        assertTrue(s.getRepresentationString().equals("xcbabax"));
        assertFalse(s.getRepresentationString().equals("xSbax"));
    }

    @Test
    public void getFIRSTTest() {
        CFGrammar grammar = new CFGrammar();
        grammar.addProduction(new CFProduction('Z', "S"));
        grammar.addProduction(new CFProduction('S', "Sb"));
        grammar.addProduction(new CFProduction('S', "bAa"));
        grammar.addProduction(new CFProduction('A', "aSc"));
        grammar.addProduction(new CFProduction('A', "a"));
        grammar.addProduction(new CFProduction('A', "aSb"));

        Sequence testSequence = new Sequence("ZSA");
        Set<Symbol> testFIRST = testSequence.getFIRST(grammar);

        assertTrue(testFIRST.contains(new TerminalSymbol('b')));
        assertTrue(testFIRST.contains(new TerminalSymbol('a')));
        assertTrue(testFIRST.size() == 2);
    }
}
