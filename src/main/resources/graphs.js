// Create a new directed graph
var g = new dagreD3.graphlib.Graph().setGraph({});

// States and transitions from RFC 793
/*var states = [ "CLOSED", "LISTEN", "SYN RCVD", "SYN SENT",
    "ESTAB", "FINWAIT-1", "CLOSE WAIT", "FINWAIT-2",
    "CLOSING", "LAST-ACK", "TIME WAIT" ];
    */
var states = [];

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

function drawGraph() {
    render(inner, g);

    //TODO draw id rects in top right corner of each state
    d3.select("g rect")
        .data(states)
        .enter()
        .each(function(d, i) {
            console.log("g rect", d, i);
        });

    // Center the graph
    var initialScale = 0.75;
    svg.call(zoom.transform, d3.zoomIdentity.translate((svg.attr("width") - g.graph().width * initialScale) / 2, 20).scale(initialScale));

    //svg.attr('height', g.graph().height * initialScale + 40);
}

function addNode(id, content) {
    console.log("Add node", id, content);
    states.push(id);

    // label node
    g.setNode(id, {id: id, label: content});

    // set style
    var node = g.node(id);
    node.rx = node.ry = 5;

    drawGraph();
}
function removeNode(id) {
    console.log("Remove node", id);

    var index = states.indexOf(id);
    if (index > -1) {
        states.splice(index, 1);
    }
    g.removeNode(id);

    drawGraph();
}
function addEdge(from, to, label) {
    console.log("Add edge", from, to, label);
    g.setEdge(from, to, {label: label});

    drawGraph();
}
function renameNode(from, to) {
    console.log("Rename node", from, to);

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