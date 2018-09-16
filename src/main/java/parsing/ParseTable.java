package parsing;

import base.Symbol;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import visualization.parseTable.ParseTableCellIdentifier;

import java.util.*;

import static parsing.ParserAction.Null;

public class ParseTable {

    private ObservableMap<Integer, ObservableMap<Symbol, TableEntry>> table; // stateNumber -> symbol -> (Entry)
    private Set<ParseTableCellIdentifier> cellsWithConflicts;

    public static class TableEntry {
        private ParserAction action;
        private Integer number;
        private Integer secondaryNumber;

        public ParserAction getAction() {
            return action;
        }

        public void setAction(ParserAction action) {
            this.action = action;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        @Override
        public String toString() {
            if(action == Null) {
                return "" + number;
            }
            String result;
            switch(action) {
                case Shift:
                    result = "s" + number;
                    break;
                case Accept:
                    result = "acc";
                    break;
                case Reduce:
                    result = "r" + number;
                    break;
                case ShiftReduceConflict:
                    result = String.format("s%d/r%d", secondaryNumber, number);
                    break;
                case ShiftShiftConflict:
                    result = String.format("s%d/s%d", secondaryNumber, number);
                    break;
                case ReduceRecudeConflict:
                    result = String.format("r%d/r%d", secondaryNumber, number);
                    break;
                default:
                    result = "-";
            }
            return result;
        }
    }

    public ParseTable() {
        table = new SimpleMapProperty<>(FXCollections.observableHashMap());
        cellsWithConflicts = new HashSet<>();
    }

    public void add(Integer stateNum, Symbol symbol, ParserAction parserAction, Integer targetState) {
        if(table.get(stateNum) == null)
            table.put(stateNum, new SimpleMapProperty<>(FXCollections.observableHashMap()));
        Map<Symbol, TableEntry> tableRow = table.get(stateNum);
        TableEntry entry;
        if(tableRow.get(symbol) == null)
            entry = new TableEntry();
        else
            entry = tableRow.get(symbol);
        if(entry.action != null) {
            if(entry.action == ParserAction.Shift && parserAction == ParserAction.Shift) {
                entry.action = ParserAction.ShiftShiftConflict;
                entry.secondaryNumber = entry.number;
                cellsWithConflicts.add(new ParseTableCellIdentifier(stateNum, symbol));
            } else if(entry.action == ParserAction.Shift && parserAction == ParserAction.Reduce) {
                entry.action = ParserAction.ShiftReduceConflict;
                entry.secondaryNumber = entry.number;
                cellsWithConflicts.add(new ParseTableCellIdentifier(stateNum, symbol));
            } else if(entry.action == ParserAction.Reduce && parserAction == ParserAction.Reduce)  {
                entry.action = ParserAction.ReduceRecudeConflict;
                entry.secondaryNumber = entry.number;
                cellsWithConflicts.add(new ParseTableCellIdentifier(stateNum, symbol));
            } else if(entry.action != Null)
                throw new IllegalArgumentException(
                        "A parse table entry with action " + entry.action + " may not be overwritten by action " + parserAction + ".");
        } else {
            entry.action = parserAction;
        }
        entry.number = targetState;

        // to trigger a change event this has to be a new object
        TableEntry newEntry = new TableEntry();
        newEntry.action = entry.action;
        newEntry.number = entry.number;
        newEntry.secondaryNumber = entry.secondaryNumber;
        tableRow.put(symbol, newEntry);
    }

    public TableEntry getEntry(int state, Symbol symbol) {
        TableEntry result = null;
        if(table.get(state) != null)
            result = table.get(state).get(symbol);
        return result;
    }

    public Collection<ObservableMap<Symbol, TableEntry>> getRows() {
        return table.values();
    }

    public void addChangeListener(MapChangeListener<Integer, ObservableMap<Symbol, TableEntry>> changeListener) {
        table.addListener(changeListener);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        int space = 7;
        SortedSet<Symbol> tableHead = new TreeSet<>();
        for(Integer row : table.keySet()) {
            tableHead.addAll(table.get(row).keySet());
        }
        result.append(" ");
        for(Symbol symbol : tableHead) {
            result.append(String.format("%" + space + "s", symbol));
        }
        result.append("\n");

        for(Integer row : table.keySet()) {
            result.append(row);
            for(Symbol symbol : tableHead) {
                if(table.get(row).containsKey(symbol)) {
                    TableEntry entry = table.get(row).get(symbol);
                    result.append(String.format("%" + space + "s", entry));
                } else {
                    result.append(String.format("%" + space + "s", "."));
                }
            }
            result.append("\n");
        }
        return result.toString();
    }

    public boolean hasConflicts() {
        return cellsWithConflicts.size() != 0;
    }

    public Set<ParseTableCellIdentifier> getCellsWithConflicts() {
        return this.cellsWithConflicts;
    }
}
