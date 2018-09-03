var grammarRules = {};
var nextSymbolId = 0;

function tspans(ruleId, str) {
    var result = "";
    for (var i = 0; i < str.length; i++) {
        var symbolId = nextSymbolId;
        result += '<span id="symbol'+symbolId+'">' + str.charAt(i) + '</span>';
        if(grammarRules[ruleId].symbols[str.charAt(i)] == null)
            grammarRules[ruleId].symbols[str.charAt(i)] = [];
        grammarRules[ruleId].symbols[str.charAt(i)].push("symbol"+symbolId);
        nextSymbolId++;
    }
    return result;
}

function generateTableRowHtml(ruleId, left, right) {
    return '<tr id="rule'+ruleId+'">'
    + '<td>' + ruleId + '</td>'
    + '<td>' + left + '&rarr;' + tspans(ruleId, right) + '</td>'
    + '</tr>';
}

// this will insert the rule at position 0 and move
// all existing rules accordingly
function insertFirstRule(left, right) {
    var rowHtml = generateTableRowHtml(0, left, right);
    $(rowHtml).insertBefore('#grammarTable > tbody > tr:first()');
    grammarRules[-1] = {left: left, right: right, symbols: {}};

    // update numbers
    $('#grammarTable > tbody > tr > td:first-child()').each(function(i, element) {
        if(i != 0) {
            var ruleId = Number(element.innerHTML);
            element.innerHTML = (ruleId+1);
        }
    });
}
function addRule(ruleId, left, right) {
    if(grammarRules[ruleId] != null) {
        $("#rule"+ruleId).remove();
        delete grammarRules[ruleId];
    }
    grammarRules[ruleId] = {left: left, right: right, symbols: {}};
    $('#grammarTable').find('tbody').append(generateTableRowHtml(ruleId, left, right));
}
function clearRules() {
    for(var id in grammarRules) {
        $("#rule"+id).remove();
    }
    grammarRules = {};
}

function highlight(selector) {
    var element = $(selector);
    if (element != null) {
        element.css('background-color', 'hsl(46,100%,50%');
        element.css('border-color', 'hsl(46,100%,50%)');
        element.css('border-style', 'solid');
        element.css('border-width', "5px 0px");
    }
    else console.error("element to highlight not found");
}

function unhighlight(selector) {
    var element = $(selector);
    if (element != null) {
        var d = 1000;
        for (var i = 50; i <= 100; i = i + 0.5) { //i represents the lightness
            d += 10;
            (function (ii, dd) {
                setTimeout(function () {
                    element.css('background-color', 'hsl(46,100%,' + ii + '%)');
                    element.css('border-color', 'hsl(46,100%,' + ii + '%)');
                    if (ii >= 99) element.css('border', '');
                }, dd);
            })(i, d);
        }
    }
    else console.error("element to unhighlight not found");
}

function highlightSymbol(ruleId, symbol, occurrence) {
    occurrence = occurrence || 0;
    highlight("#" + grammarRules[ruleId].symbols[symbol][occurrence]);
}

function unhighlightSymbol(ruleId, symbol, occurrence) {
    occurrence = occurrence || 0;
    unhighlight("#" + grammarRules[ruleId].symbols[symbol][occurrence]);
}

function highlightRule(ruleId) {
    highlight("#rule" + ruleId);
}

function unhighlightRule(ruleId) {
    unhighlight("#rule" + ruleId);
}
