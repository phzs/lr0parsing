// Create a new directed graph
var g = new dagreD3.graphlib.Graph().setGraph({nodesep: 70});
// States and transitions from RFC 793
/*var states = [ "CLOSED", "LISTEN", "SYN RCVD", "SYN SENT",
    "ESTAB", "FINWAIT-1", "CLOSE WAIT", "FINWAIT-2",
    "CLOSING", "LAST-ACK", "TIME WAIT" ];
    */
var states = [];
var stateNumRectSize = 30;

// Automatically label each of the nodes
states.forEach(function(state) { g.setNode(state, { label: state }); });

// Set up the edges
/*
g.setEdge("CLOSED",     "LISTEN",     { label: "open" });
g.setEdge("LISTEN",     "SYN RCVD",   { label: "rcv SYN" });
g.setEdge("LISTEN",     "SYN SENT",   { label: "send" });
g.setEdge("LISTEN",     "CLOSED",     { label: "close" });
g.setEdge("SYN RCVD",   "FINWAIT-1",  { label: "close" });
g.setEdge("SYN RCVD",   "ESTAB",      { label: "rcv ACK of SYN" });
g.setEdge("SYN SENT",   "SYN RCVD",   { label: "rcv SYN" });
g.setEdge("SYN SENT",   "ESTAB",      { label: "rcv SYN, ACK" });
g.setEdge("SYN SENT",   "CLOSED",     { label: "close" });
g.setEdge("ESTAB",      "FINWAIT-1",  { label: "close" });
g.setEdge("ESTAB",      "CLOSE WAIT", { label: "rcv FIN" });
g.setEdge("FINWAIT-1",  "FINWAIT-2",  { label: "rcv ACK of FIN" });
g.setEdge("FINWAIT-1",  "CLOSING",    { label: "rcv FIN" });
g.setEdge("CLOSE WAIT", "LAST-ACK",   { label: "close" });
g.setEdge("FINWAIT-2",  "TIME WAIT",  { label: "rcv FIN" });
g.setEdge("CLOSING",    "TIME WAIT",  { label: "rcv ACK of FIN" });
g.setEdge("LAST-ACK",   "CLOSED",     { label: "rcv ACK of FIN" });
g.setEdge("TIME WAIT",  "CLOSED",     { label: "timeout=2MSL" });
*/
// Set some general styles
g.nodes().forEach(function(v) {
    var node = g.node(v);
    node.rx = node.ry = 5;
    node.width += 50;
});

// Add some custom colors based on state
//g.node('CLOSED').style = "fill: #f77";
//g.node('ESTAB').style = "fill: #7f7";

var svg = d3.select("svg"),
    inner = svg.select("g");

// Set up zoom support
var zoom = d3.zoom().on("zoom", function() {
    inner.attr("transform", d3.event.transform);
});
svg.call(zoom);

// Create the renderer
var render = new dagreD3.render();

// Run the renderer. This is what draws the final graph.
if(states.length > 0)
    drawGraph();

function calcYoffset(height) {
    var H = height,
        RR = 2*stateNumRectSize;
    if(H == RR) {
        return 0;
    } else if(H > RR) {
        return ((H/2.0) - stateNumRectSize);
    } else if(H < RR) {
        return -(stateNumRectSize - (H/2.0));
    }
}

function drawGraph() {

    render(inner, g);

    d3.selectAll(".node > rect")
        .attr("width", function(d) {
            return Number(d3.select(this).attr("width")) + (stateNumRectSize + 2);
        });

    var offset = 16;
    d3.selectAll(".node > rect") // '>' to select only the first rect in <g class="node">...</g>
        .data(states)
        .attr("width", function(d) {
            var node_height, node_width;
            node_width = Number(d3.select(this).attr("width"));
            node_height = Number(d3.select(this).attr("height"));

            var g = d3.select(this.parentNode)
                .append("g")
                .attr("class", "stateNum")
                .attr("transform", function() {
                    return "translate("+((node_width/2) - (stateNumRectSize) + offset)+","+calcYoffset(node_height)+")";
                });
            g.append("rect")
                .attr("width", stateNumRectSize)
                .attr("height", stateNumRectSize)
                .style("fill", "#f7f7f7");
            g.append("g")
                .attr("class", "label")
                .attr("transform", function() {
                    return "translate(0,"+2+")";
                })
                .append("g")
                .append("text")
                .append("tspan")
                .attr("space", "preserve")
                .attr("dy", "1em")
                .attr("x", function () {
                    if(Number(d.id+5) < 10) return 9;
                    else return 4;
                })
                .text(function(){ return d.id+5 });
            return node_width;
        });
    // Center the graph
    var initialScale = 0.75;
    svg.call(zoom.transform, d3.zoomIdentity.translate((svg.attr("width") - g.graph().width * initialScale) / 2, 20).scale(initialScale));

    //svg.attr('height', g.graph().height * initialScale + 40);
}

function addNode(id, content) {
    states.push(
        {
            id: id,
            content: content
        }
    );

    // label node
    g.setNode(id, {id: id, label: content});

    // set style
    var node = g.node(id);

    drawGraph();
}
function removeNode(id) {

    var index = states.indexOf(id);
    if (index > -1) {
        states.splice(index, 1);
    }
    g.removeNode(id);

    drawGraph();
}
function addEdge(from, to, label) {
    g.setEdge(from, to, {label: label});

    drawGraph();
}
function renameNode(from, to) {

    var index = states.indexOf(from);
    if(index > -1) {
        states[index] = to;
        g.node(from).label = to;
        drawGraph();
    }
}

function clearGraph() {
    var toRemove = [];
    states.forEach(function(stateId) {
       toRemove.push(stateId);
    });
    toRemove.forEach(function(stateId) {
       removeNode(stateId);
    });
}