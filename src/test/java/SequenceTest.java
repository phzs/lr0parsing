import org.junit.jupiter.api.Test;
import parsing.CFProduction;
import parsing.Sequence;

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
}
