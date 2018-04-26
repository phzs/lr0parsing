package parsing;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LR0ElementTest {

    @Test
    public void toStringTest() {
        CFProduction cfProduction = new CFProduction('A', "abc");
        LR0Element lr0Element = new LR0Element(cfProduction, 0);
        assertTrue(lr0Element.toString().equals("[A --> .abc]"));
    }

    @Test
    public void equalityTest() {
        CFProduction cfProduction = new CFProduction('A', "abc");
        for (int i = 0; i < cfProduction.getRight().getLength(); i++) {
            LR0Element a = new LR0Element(cfProduction, i);
            LR0Element b = new LR0Element(new CFProduction('A', "abc"), i);
            assertTrue(a.equals(b));
            assertTrue(a.hashCode() == b.hashCode());
        }
        CFProduction cfProduction1 = new CFProduction('B', "Foo");
        LR0Element a = new LR0Element(cfProduction, 0);
        LR0Element b = new LR0Element(cfProduction1, 0);
        LR0Element c = new LR0Element(cfProduction1, 1);
        assertFalse(a.equals(b));
        assertFalse(b.equals(c));

        Set<LR0Element> set = new HashSet<>();
        set.add(a);
        assertFalse(set.add(a));
    }
}