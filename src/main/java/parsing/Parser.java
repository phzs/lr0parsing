package parsing;

import base.CFGrammar;

public interface Parser {
    StateAutomaton parse(CFGrammar grammar);

    SyntaxAnalysisTable generateTable(CFGrammar grammar, StateAutomaton stateAutomaton);
}
