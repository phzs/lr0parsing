import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import parsing.CFGrammar;
import parsing.CFProduction;
import parsing.MetaSymbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CFGrammarTest {
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
