package visualization;

import base.*;

import java.util.List;

public final class JsUtil {

    private static String listToJsArray(List<? extends Symbol> list) {
        String result = "[";
        for(Symbol symbol : list) {
            result += ("\"" + symbol.toString() + "\", ");
        }
        result += "]";
        return result;
    }

    public static void initParseTable(View view, List<TerminalSymbol> terminalSymbols, List<MetaSymbol> metaSymbols) {
        String script = "initParseTable("
                + listToJsArray(terminalSymbols)
                + ", "
                + listToJsArray(metaSymbols)
                +")";
        view.executeScript(script);
    }

    public static void initGrammar(View view, CFGrammar grammar) {
        view.executeScript("clearRules()");
        List<CFProduction> productionList = grammar.getProductionList();
        for(int i = 0; i < productionList.size(); i++) {
            CFProduction production = productionList.get(i);
            String script = "addRule(" + i + ", \"" + production.getLeft() + "\", \"" + production.getRight()+"\")";
            view.executeScript(script);
        }
    }
}
