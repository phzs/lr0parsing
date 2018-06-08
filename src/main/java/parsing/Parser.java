package parsing;

import base.CFGrammar;

public interface Parser {

    StateAutomaton getStateAutomaton();
    void setStateAutomaton(StateAutomaton stateAutomaton);
    void parse(CFGrammar grammar);

    ParseTable getParseTable();
    void setParseTable(ParseTable parseTable);
    ParseTable generateTable(CFGrammar grammar, StateAutomaton stateAutomaton);
}
