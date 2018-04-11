package test;

import org.testng.Assert;
import org.testng.annotations.Test;
import parsing.CFProduction;
import parsing.Sequence;

public class SequenceTest {
    @Test
    public void leftmostDerivationTest() {
        Sequence s = new Sequence("xSbax");
        Assert.assertTrue(s.applyLeftmostDerivation(new CFProduction('S', "cba")));

    }
}
