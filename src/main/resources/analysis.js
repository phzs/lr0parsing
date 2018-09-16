var stackItems = [];
var nextStackId = 0;

function getIdForStackItem(id) {
    return "stackItem"+id;
}

function generateStackItemHtml(id, symbol) {
    return "<div id=\""+getIdForStackItem(id)+"\" class=\"col-1 stackItem\">"+symbol+"</div>";
}

function stackPush(symbol) {
    stackItems.push(symbol);
    $('#stackCanvas').append(generateStackItemHtml(nextStackId++, symbol));
}

function stackPop() {
    stackItems.pop();
    $('#stackCanvas > div:last').remove();
    nextStackId--;
}

function stackClear() {
    $('#stackCanvas').empty();
}

function setResult(result, mode) {
    var resultBox = $('#analysisResultBox');
    resultBox.html(result);
    var styleClass;
    if(mode === 0) styleClass = "highlighted-green";
    else if(mode === 1) styleClass = "highlighted";
    else if(mode === 2) styleClass = "highlighted-red";
    resultBox.addClass(styleClass);
    $('#analysisTableRow').hide();
    $('#analysisResultRow').show();
}
function resetResult() {
    var resultBox = $('#analysisResultBox');
    $('#analysisResultRow').hide();
    $('#analysisTableRow').show();
    resultBox.html("");
    resultBox.hasClass();
    resultBox.removeClass(["highlighted", "highlighted-red"]);
}

function resetAnalysis() {
    resetResult();
    stackClear();
    $('#analyisErrorBox').hide();
}

function setInput(input) {
    $('#inputDeleted').html("");
    $('#inputNext').html(input.charAt(0));
    $('#inputAfter').html(input.substring(1));
}

function resetInput() {
    $('#inputDeleted').html("");
    $('#inputNext').html("");
    $('#inputAfter').html("");
}

function moveInputSymbol() {
    var after = $('#inputAfter');
    var nextSym = after.html().charAt(0);
    if(nextSym != null) {
        var last = $('#inputNext');
        var lastSymbol = last.html().charAt(0);
        var deleted = $('#inputDeleted');
        var deletedSymbols = deleted.html();
        deleted.html(deletedSymbols + lastSymbol);
        last.html(nextSym);
        var nextAfter = after.html().substring(1);
        after.html(nextAfter);
    }
}

function showAnalysisError() {
    $('#analyisErrorBox').show();
}

function highlightInputNext() {
    $('#inputNext').addClass("highlighted");
}
function unhighlightInputNext() {
    $('#inputNext').removeClass("highlighted");
}