var gcSymbols = {};
var nextGcSymbolId = 0;

function getGcSymbolId(idNum) {
    return "gcSymbol"+idNum;
}

function gcTspans(entry) {
    var marker_pos = entry.indexOf('.');
    var followSym = entry[marker_pos+1];
    if(marker_pos !== -1 && followSym != null && " ][".indexOf(followSym) === -1 ) {
        var result = entry.replace("." + followSym, ".<span id=\'"+getGcSymbolId(nextGcSymbolId)+"\'>"+followSym+"</span>");
        if(gcSymbols[followSym] == null)
            gcSymbols[followSym] = [];
        gcSymbols[followSym].push(nextGcSymbolId);
        nextGcSymbolId++;
        return result;
    } else
        return entry;
}

function setGcGOTO(symbol) {
    $('#gcTitle').html("GOTO<sub>0</sub>(..., "+symbol+")");
}
function setGcCLOSURE() {
    var title = $('#gcTitle');
    if(title.html() === "")
        title.html("CLOSURE<sub>0</sub>(...)");
}

function addGcEntry(entry) {
    var entryHTML = "<li>" + gcTspans(entry) + "</li>";
    $('#gcDisplay').append(entryHTML);
}
function addGcLine() {
    $('#gcDisplay').append("<hr/>");
}

function clearGc() {
    $('#gcTitle').html("");
    $('#gcDisplay').empty();
    gcSymbols = {};
}

function highlightGcSymbols(symbol, color) {
    gcSymbols[symbol].forEach(function(id) {
        $('#'+getGcSymbolId(id)).css("background-color", color);
    });
}

function highlightAllGcSymbols(color) {
    Object.keys(gcSymbols).forEach(function(key) {
        if(key == key.toLowerCase())
            highlightGcSymbols(key, "#d1d1d1");
        else
            highlightGcSymbols(key, color);
    });
}