import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import parsing.CFGrammar;
import parsing.CFProduction;

import java.io.IOException;

public class CFGrammarTest {
    private static final String simpleGrammarJSON = "{\"productionList\":[{\"left\":{\"representation\":\"A\"},\"right\":[{\"representation\":\"a\"},{\"representation\":\"A\"},{\"representation\":\"b\"}]},{\"left\":{\"representation\":\"S\"},\"right\":[{\"representation\":\"A\"}]}]}";

    @Test
    public void saveGrammarAsJsonTest() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String expectedJSON = simpleGrammarJSON;
        CFGrammar CFGrammar = new CFGrammar();
        CFGrammar.addProduction(new CFProduction('A', "aAb"));
        CFGrammar.addProduction(new CFProduction('S', "A"));
        String grammarJSON = objectMapper.writeValueAsString(CFGrammar);
        assertEquals(grammarJSON, expectedJSON);
    }

    @Test
    public void restoreGrammarFromJsonTest() throws JsonProcessingException, IOException {
        //TODO
    }
}
