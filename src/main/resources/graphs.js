// Create a new directed graph
var g = new dagreD3.graphlib.Graph().setGraph({nodesep: 70});

var states = [];
var stateNumRectSize = 30;

// Automatically label each of the nodes
states.forEach(function(state) { g.setNode(state, { label: state }); });

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
    if(H === RR) {
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
        .attr("width", function() {
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
                .text(function(){ return d.id });
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