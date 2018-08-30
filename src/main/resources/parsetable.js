var actionTableHeaders = [];
var jumpTableHeaders = [];
var parseTableRows = {};

function initParseTable(terminalSymbols, metaSymbols) {
    $('#tableHead').append("<th></th>");
    terminalSymbols.forEach(function(tsym) {
        $('#tableHead').append("<th id=\'actionTableHeader_"+tsym+"\'>"+tsym+"</th>");
        actionTableHeaders.push(tsym);
    });
    metaSymbols.forEach(function(msym) {
        $('#tableHead').append("<th id=\'jumpTableHeader_"+msym+"\'>"+msym+"</th>");
        jumpTableHeaders.push(msym);
    });
    $('#actionTableHeader').attr("colspan", actionTableHeaders.length);
    $('#jumpTableHeader').attr("colspan", jumpTableHeaders.length);
}

function getIdForCell(stateId, symbol) {
    return "cell"+stateId+"_"+symbol.charCodeAt(0);
}

function addParseTableEntry(stateId, symbol, entry) {
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
