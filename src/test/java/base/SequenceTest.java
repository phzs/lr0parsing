package base;

import base.CFProduction;
import base.MetaSymbol;
import base.Sequence;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    public void subsequenceTest() {
        Sequence s = new Sequence("xSbax");
        assertTrue(s.subsequence(0).equals(s));
        assertTrue(s.subsequence(2).toString().equals("bax"));
        assertTrue(s.subsequence(0,1).toString().equals("x"));
        assertTrue(s.subsequence(1,4).toString().equals("Sba"));
        assertTrue(s.subsequence(0,5).equals(s));
    }

    @Test
    public void getMetaSymbolsTest() {
        Sequence sequence1 = new Sequence("AbCdEf");
        List<MetaSymbol> result1 = sequence1.getMetaSymbols();
        assertTrue(result1.contains(new MetaSymbol('A')));
        assertTrue(result1.contains(new MetaSymbol('C')));
        assertTrue(result1.contains(new MetaSymbol('E')));
        assertTrue(result1.size() == 3);

        Sequence sequence2 = new Sequence("xlFGdfH");
        List<MetaSymbol> result2 = sequence2.getMetaSymbols();
        assertTrue(result2.contains(new MetaSymbol('F')));
        assertTrue(result2.contains(new MetaSymbol('G')));
        assertTrue(result2.contains(new MetaSymbol('H')));
        assertTrue(result2.size() == 3);
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
