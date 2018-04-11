package test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import parsing.CFGrammar;
import parsing.CFProduction;

public class CFGrammarTest {
    private ObjectMapper objectMapper;

    @BeforeTest
    public void init() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void saveGrammarAsJson() throws JsonProcessingException {
        CFGrammar CFGrammar = new CFGrammar();
        CFGrammar.addProduction(new CFProduction('A', "aAb"));
        CFGrammar.addProduction(new CFProduction('S', "A"));
        String grammarJSON = objectMapper.writeValueAsString(CFGrammar);
        System.out.println("grammarJSON");
    }
}
