var actionTableHeaders = [];
var jumpTableHeaders = [];
var parseTableRows = {};

function testId(id) {try { $("#"+id).length != null; return true; } catch(e) { return false; }}

function getIdForParseTableHeader(table, sym) {
    if(!testId(table + "_" +  sym))
        sym = sym.charCodeAt(0);
    return table + "_" +  sym;
}

function initParseTable(terminalSymbols, metaSymbols) {
    $('#tableHead').append("<th></th>");
    terminalSymbols.forEach(function(tsym) {
        var id = getIdForParseTableHeader("actionTableHeader", tsym);
        $('#tableHead').append("<th id=\'"+id+"\'>"+tsym+"</th>");
        actionTableHeaders.push(tsym);
    });
    metaSymbols.forEach(function(msym) {
        var id = getIdForParseTableHeader("jumpTableHeader", msym);
        $('#tableHead').append("<th id=\'"+id+"\'>"+msym+"</th>");
        jumpTableHeaders.push(msym);
    });
    $('#actionTableHeader').attr("colspan", actionTableHeaders.length);
    $('#jumpTableHeader').attr("colspan", jumpTableHeaders.length);
}

function getIdForCell(stateId, symbol) {
    return "cell"+stateId+"_"+symbol.charCodeAt(0);
}

function addParseTableEntry(stateId, symbol, entry) {
    if(stateId > 0 && parseTableRows[stateId-1] == null)
        addParseTableEntry(stateId-1, actionTableHeaders[0], "");
    if(!(parseTableRows[stateId] != null)) {
        var cells = "<td>"+stateId+"</td>";
        actionTableHeaders.forEach(function(tsym) {
            cells += "<td id=\'"+getIdForCell(stateId, tsym)+"\'>";
        });
        jumpTableHeaders.forEach(function(msym) {
            cells += "<td id=\'"+getIdForCell(stateId, msym)+"\'></td>";
        });
        var row = "<tr id=\'row"+stateId+"\'>"+cells+"</tr>";
        $('#parseTable').find("tbody").append(row);
        parseTableRows[stateId] = {};
    }
    $('#'+getIdForCell(stateId, symbol)).html(entry);
    parseTableRows[stateId][symbol] = entry;
}

function clearParseTable() {
    $('#tableHead > th').remove();
    Object.keys(parseTableRows).forEach(function(key) {
        $('#row'+key).remove()
    });
    actionTableHeaders = [];
    jumpTableHeaders = [];
    parseTableRows = {};
}

function highlightParseTableRow(stateId, styleClass) {
    var styleClass = styleClass || "highlighted";
    $('#row'+stateId+" > td:first").addClass(styleClass);

}

function unhighlightParseTableRow(stateId) {
    $('#row'+stateId+" > td:first").removeClassPrefix("highlighted");
}

function highlightParseTableHeader(symbol, _color) {
    var color = _color || null;
    var styleClass = "highlighted-blue"; // default
    if(color != null)
        styleClass = styleClass.substring(0, styleClass.indexOf("-")+1) + color;
    if(jumpTableHeaders.indexOf(symbol) !== -1)
        $('#'+getIdForParseTableHeader("jumpTableHeader",symbol)).addClass(styleClass);
    else if(actionTableHeaders.indexOf(symbol) !== -1)
        $('#'+getIdForParseTableHeader("actionTableHeader",symbol)).addClass(styleClass);
}

function unhighlightParseTableHeader(symbol) {
    if(jumpTableHeaders.indexOf(symbol) !== -1)
        $('#'+getIdForParseTableHeader("jumpTableHeader",symbol)).removeClassPrefix("highlighted");
    else if(actionTableHeaders.indexOf(symbol) !== -1)
        $('#'+getIdForParseTableHeader("actionTableHeader",symbol)).removeClassPrefix("highlighted");
}

function highlightParseTableCell(stateId, symbol, styleClass) {
    var styleClass = styleClass || "highlighted-red";
    $('#'+getIdForCell(stateId, symbol)).addClass(styleClass);
}
function unhighlightParseTableCell(stateId, symbol) {
    $('#'+getIdForCell(stateId, symbol)).removeClassPrefix("highlighted");
}