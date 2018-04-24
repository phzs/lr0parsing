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
        assertTrue(s.toString().equals("xcbabax"));
        assertFalse(s.toString().equals("xSbax"));
    }

    @Test
    public void equalityTest() {
        String representationString = "abcScba";
        Sequence a = new Sequence(representationString);
        Sequence b = new Sequence(representationString);
        assertTrue(a.equals(b));
        assertTrue(a.hashCode() == b.hashCode());

        Sequence c = new Sequence("abccba");
        Sequence d = new Sequence("abc");
        Sequence e = new Sequence(representationString + "foo");
        assertFalse(a.equals(c) || a.hashCode() == c.hashCode());
        assertFalse(a.equals(d) || a.hashCode() == d.hashCode());
        assertFalse(a.equals(e) || a.hashCode() == d.hashCode());
    }
}
