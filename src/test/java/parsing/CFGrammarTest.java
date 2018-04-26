package parsing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CFGrammarTest {

    /**
     * Get the example grammar from Andreas Kunert's publication
     * http://amor.cms.hu-berlin.de/~kunert/papers/lr-analyse/
     * @return CFGrammar which can be used for tests
     */
    public static CFGrammar getExampleGrammar() {
        CFGrammar exampleGrammar = new CFGrammar('S');
        exampleGrammar.addProduction(new CFProduction('S', "Sb"));
        exampleGrammar.addProduction(new CFProduction('S', "bAa"));
        exampleGrammar.addProduction(new CFProduction('A', "aSc"));
        exampleGrammar.addProduction(new CFProduction('A', "a"));
        exampleGrammar.addProduction(new CFProduction('A', "aSb"));
        return exampleGrammar;
    }
    public static CFGrammar getExampleGrammar2() {
        // same as example grammar one but with additional starting production and symbol Z
        CFGrammar exampleGrammar = new CFGrammar('Z');
        exampleGrammar.addProduction(new CFProduction('Z', "S"));
        exampleGrammar.addProduction(new CFProduction('S', "Sb"));
        exampleGrammar.addProduction(new CFProduction('S', "bAa"));
        exampleGrammar.addProduction(new CFProduction('A', "aSc"));
        exampleGrammar.addProduction(new CFProduction('A', "a"));
        exampleGrammar.addProduction(new CFProduction('A', "aSb"));
        return exampleGrammar;
    }

    @Test
    public void jsonSerializationTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enableDefaultTyping(
                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        CFGrammar cFGrammar = new CFGrammar('S');

        cFGrammar.setProductionList(new ArrayList<>());
        cFGrammar.addProduction(new CFProduction('A', "aAb"));
        cFGrammar.addProduction(new CFProduction('S', "A"));
        String grammarJSON = objectMapper.writeValueAsString(cFGrammar);

        CFGrammar cfGrammar2 = objectMapper.readValue(grammarJSON, CFGrammar.class);

        List<CFProduction> productionList = cfGrammar2.getProductionList();
        assertTrue(cfGrammar2.getStartSymbol() != null);
        assertTrue(cfGrammar2.getStartSymbol().getRepresentation() == 'S');

        assertTrue(productionList.contains(new CFProduction('A', "aAb")));
        assertTrue(productionList.contains(new CFProduction('S', "A")));
        assertTrue(productionList.size() == 2);
    }

    @Test
    public void getCLOSURETest() {
        CFGrammar grammar = CFGrammarTest.getExampleGrammar2();
        Set<LR0Element> expectedResult = new HashSet<>();
        CFProduction firstProduction = grammar.getProductionList().get(0);
        expectedResult.add(new LR0Element('Z', "S", 0));
        expectedResult.add(new LR0Element('S', "Sb", 0));
        expectedResult.add(new LR0Element('S', "bAa", 0));
        Set<LR0Element> closure = grammar.getCLOSURE(firstProduction.getLR0Element(0));
        assertTrue(closure.containsAll(expectedResult));
        assertTrue(closure.size() == expectedResult.size());

        Set<LR0Element> expectedResult2 = new HashSet<>();
        expectedResult2.add(new LR0Element('S', "bAa", 1));
        expectedResult2.add(new LR0Element('A', "aSc", 0));
        expectedResult2.add(new LR0Element('A', "a", 0));
        expectedResult2.add(new LR0Element('A', "aSb", 0));
        Set<LR0Element> closure2 = grammar.getCLOSURE(new LR0Element('S', "bAa", 1));
        assertTrue(closure2.containsAll(expectedResult2));
        assertTrue(closure2.size() == expectedResult2.size());
    }

    @Test
    public void getGOTOTest() {
        CFGrammar grammar = CFGrammarTest.getExampleGrammar2();
        Set<LR0Element> elements = new HashSet<>();
        elements.add(new LR0Element('Z', "S", 0));
        elements.add(new LR0Element('S', "Sb", 0));
        elements.add(new LR0Element('S', "bAa", 0));
        Set<LR0Element> expectedResult = new HashSet<>();
        expectedResult.add(new LR0Element('S', "bAa", 1));
        expectedResult.add(new LR0Element('A', "aSc", 0));
        expectedResult.add(new LR0Element('A', "a", 0));
        expectedResult.add(new LR0Element('A', "aSb", 0));
        Set<LR0Element> result = grammar.getGOTO(elements, new TerminalSymbol('b'));
        assertTrue(result.containsAll(expectedResult));
        assertTrue(result.size() == expectedResult.size());
    }

    @Test
    public void equalityTest() {
        CFGrammar a = new CFGrammar('S');
        a.addProduction(new CFProduction('S', "a"));

        CFGrammar b = new CFGrammar('S');
        b.addProduction(new CFProduction('S', "a"));

        assertTrue(a.equals(b) && a.hashCode() == b.hashCode());

        CFGrammar c = new CFGrammar('A');
        c.addProduction(new CFProduction('A', "a"));

        assertFalse(a.equals(c) || a.hashCode() == c.hashCode());
    }
}
