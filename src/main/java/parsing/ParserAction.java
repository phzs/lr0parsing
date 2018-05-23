package parsing;

public enum ParserAction {
    Null,
    Shift,
    Accept,
    Reduce,
    ShiftReduceConflict,
    ShiftShiftConflict,
    ReduceRecudeConflict
}
