package parsing;

import base.CFGrammar;

public interface Parser {

    public StateAutomaton getStateAutomaton();
    public void setStateAutomaton(StateAutomaton stateAutomaton);
    void parse(CFGrammar grammar);

    public ParseTable getParseTable();
    public void setParseTable(ParseTable parseTable);
    ParseTable generateTable(CFGrammar grammar, StateAutomaton stateAutomaton);
}
