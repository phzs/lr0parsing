var grammarRules = {};
var nextSymbolId = 0;

function tspans(ruleId, str) {
    var result = "";
    for (var i = 0; i < str.length; i++) {
        var symbolId = nextSymbolId;
        result += '<span id="symbol'+symbolId+'">' + str.charAt(i) + '</span>';
        grammarRules[ruleId].symbols[str.charAt(i)] = "symbol"+symbolId;
        nextSymbolId++;
    }
    return result;
}

function addRule(ruleId, left, right) {
    if(grammarRules[ruleId] != null) {
        $("#rule"+ruleId).remove();
        delete grammarRules[ruleId];
    }
    grammarRules[ruleId] = {left: left, right: right, symbols: {}};
    $('#grammarTable').find('tbody').append(
      '<tr id="rule'+ruleId+'">'
        + '<td>' + ruleId + '</td>'
        + '<td>' + left + '&rarr;' + tspans(ruleId, right) + '</td>'
        + '</tr>'
    );
}
function clearRules() {
    for(var id in grammarRules) {
        $("#rule"+id).remove();
    }
    grammarRules = {};
}

function highlightSymbol(ruleId, symbol) {
    var span = $("#" + grammarRules[ruleId].symbols[symbol]);
    if (span != null) {
        console.log("span", span);
        span.css('background-color', 'hsl(46,100%,50%');
        span.css('border', '5px solid hsl(46,100%,50%)');
        var d = 1000;
        for (var i = 50; i <= 100; i = i + 0.1) { //i represents the lightness
            d += 10;
            (function (ii, dd) {
                setTimeout(function () {
                    span.css('background-color', 'hsl(46,100%,' + ii + '%)');
                    span.css('border', '5px solid hsl(46,100%,' + ii + '%)');
                    if (ii >= 99) span.css('border', '');
                }, dd);
            })(i, d);
        }
    }
    else console.error("span for symbol not found");
}
